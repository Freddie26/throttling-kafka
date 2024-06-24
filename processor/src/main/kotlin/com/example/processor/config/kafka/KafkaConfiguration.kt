package com.example.processor.config.kafka

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.transaction.KafkaTransactionManager
import java.util.UUID

@Configuration
class KafkaConfiguration(
    private val kafkaProperties: KafkaProperties,
) {

    @Bean
    fun producerFactory(): ProducerFactory<String, String> =
        DefaultKafkaProducerFactory(
            mutableMapOf<String, Any?>(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers,

                ProducerConfig.LINGER_MS_CONFIG to 0,
                ProducerConfig.RETRIES_CONFIG to 3,
                ProducerConfig.ACKS_CONFIG to "all",
                ProducerConfig.TRANSACTIONAL_ID_CONFIG to "throttled-topic-processor-${UUID.randomUUID()}",
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true,
                ProducerConfig.COMPRESSION_TYPE_CONFIG to "zstd",

                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            )
        )

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, String>) = KafkaTemplate(producerFactory)

    @Bean
    fun kafkaTransactionManager(producerFactory: ProducerFactory<String, String>) =
        KafkaTransactionManager(producerFactory)
            .also {
                it.isNestedTransactionAllowed = false
                it.isValidateExistingTransaction = true
            }
}
