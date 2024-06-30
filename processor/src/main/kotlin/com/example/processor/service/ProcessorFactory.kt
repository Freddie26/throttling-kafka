package com.example.processor.service

import com.example.processor.config.ProcessorProperties
import com.example.processor.config.kafka.KafkaProperties
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerConfig.DEFAULT_ISOLATION_LEVEL
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class ProcessorFactory(
    private val processorProperties: ProcessorProperties,
    private val kafkaProperties: KafkaProperties,
    private val bucketFactory: BucketFactory,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val throttledMessagesRegistry: ThrottledMessagesRegistry,
) {

    fun create(fromTopic: String, toTopic: String): List<TopicProcessor> {
        val throttler = createThrottler(fromTopic)

        return List(processorProperties.processorsPerTopic) {
            TopicProcessor(
                fromTopic = fromTopic,
                toTopic = toTopic,
                consumerFactory = { consumerFactory(fromTopic).createConsumer() },
                kafkaTemplate = kafkaTemplate,
                throttler = throttler,
                throttledMessagesRegistry = throttledMessagesRegistry,
            )
        }
    }

    private fun createThrottler(topic: String) =
        Throttler(
            bucketFactory.createBucket(topic),
            processorProperties.tokensLimitToConsume,
        )

    private fun consumerFactory(topic: String): ConsumerFactory<String, String> =
        DefaultKafkaConsumerFactory(
            mutableMapOf<String, Any>(
                CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers!!,
                CommonClientConfigs.GROUP_ID_CONFIG to "throttled-consumer-$topic",

                ConsumerConfig.MAX_POLL_RECORDS_CONFIG to kafkaProperties.maxPollRecords,
                ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG to 3 * 60 * 60 * 1000, // 3 часа
                ConsumerConfig.ISOLATION_LEVEL_CONFIG to DEFAULT_ISOLATION_LEVEL,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,

                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            )
        )
}
