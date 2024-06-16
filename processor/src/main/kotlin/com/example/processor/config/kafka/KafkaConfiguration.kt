package com.example.processor.config.kafka

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import java.util.UUID

@Configuration
class KafkaConfiguration(
    private val kafkaProperties: KafkaProperties,
) {

    @Bean
    fun producerFactory(): ProducerFactory<String, String> {
        val props = mutableMapOf<String, Any?>()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaProperties.bootstrapServers

        props[ProducerConfig.LINGER_MS_CONFIG] = 0
        props[ProducerConfig.RETRIES_CONFIG] = 3
        props[ProducerConfig.ACKS_CONFIG] = "all"
        props[ProducerConfig.TRANSACTIONAL_ID_CONFIG] = "speed-limiter-${UUID.randomUUID()}"
        props[ProducerConfig.COMPRESSION_TYPE_CONFIG] = "zstd"

        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java

        return DefaultKafkaProducerFactory(props)
    }

    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> {
        val props = mutableMapOf<String, Any?>()
        props[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = kafkaProperties.bootstrapServers
        props[CommonClientConfigs.GROUP_ID_CONFIG] = "" // TODO: add consumer group

        props[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = kafkaProperties.maxPollRecords
        props[ConsumerConfig.ISOLATION_LEVEL_CONFIG] = kafkaProperties.isolationLevel
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false

        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java

        return DefaultKafkaConsumerFactory(props, StringDeserializer(), StringDeserializer())
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, String>) = KafkaTemplate(producerFactory)
}
