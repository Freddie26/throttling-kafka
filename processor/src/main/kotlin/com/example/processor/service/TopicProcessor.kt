package com.example.processor.service

import com.example.processor.utils.logger
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.springframework.kafka.core.KafkaTemplate
import java.time.Duration

class TopicProcessor(
    private val fromTopic: String,
    private val toTopic: String,
    private val consumerFactory: () -> Consumer<String, String>,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val throttler: Throttler,
    private val throttledMessagesRegistry: ThrottledMessagesRegistry,
) : Runnable, StoppableTask {

    private val consumer = ThreadLocal.withInitial { consumerFactory() }.get()
        .also { it.subscribe(listOf(fromTopic)) }

    @Volatile
    private var stopped = false

    override fun run() {
        try {
            process()
        } catch (e: Exception) {
            logger().error("An error occurred during processing: ${e.message}. Processor was stopped", e)
        } finally {
            dispose()
        }
    }

    private fun process() {
        while (!stopped) {
            try {
                consumeAndProcess()
            } catch (e: InterruptedException) {
                stop()
                logger().info("Processing for the topic '$fromTopic' was interrupted: ${e.message}", e)
            } catch (e: Throwable) {
                logger().error("An error occurred during processing of the dynamic topic \"$fromTopic\": ${e.message}", e)
            }
            Thread.sleep(100)
        }

        logger().info("Processor ($this) for topic '$fromTopic' was stopped")
    }

    private fun consumeAndProcess() {
        val messages = consumer.poll(Duration.ofSeconds(1)).toList()

        logger().info("Consumed ${messages.count()} from $fromTopic")

        // разбиваем пачку сообщений на пакеты, которые можем обработать (должно хватать токенов)
        throttler.withThrottling(messages) { batch ->
            try {
                // оборачиваем в транзакцию
                kafkaTemplate.executeInTransaction {
                    // отправляем коммуникации в следующий топик
                    batch.forEach { kafkaTemplate.send(toTopic, it.value()) }

                    // собираем оффсеты для коммита
                    val offsets = mutableMapOf<TopicPartition, OffsetAndMetadata>()
                    messages.forEach { record ->
                        val partition = TopicPartition(record.topic(), record.partition())
                        val offset = OffsetAndMetadata(record.offset() + 1)
                        offsets[partition] = offset
                    }

                    // коммитим оффсеты
                    kafkaTemplate.sendOffsetsToTransaction(offsets, consumer.groupMetadata())
                }

                throttledMessagesRegistry.increment(fromTopic, batch.size)
            } catch (e : Exception) {
                // TODO: вычислить незакоммиченные оффсеты и откатиться до них чтобы повторно вычитать. Для этого
                //  стоит хранить оффсеты и смещаться при успешной обработке.
                //  Альтернатива - вечный ретрай, а это очень плохо в проде.
                // consumer.seek(..., ...)
                throw e
            }
        }
    }

    private fun dispose() {
        consumer.unsubscribe()
        consumer.close()
    }

    override fun stop() {
        stopped = true
    }
}
