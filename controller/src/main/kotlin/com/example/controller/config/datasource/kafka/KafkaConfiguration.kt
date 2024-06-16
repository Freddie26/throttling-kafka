package com.example.controller.config.datasource.kafka

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
    fun kafkaTemplate(producerFactory: ProducerFactory<String, String>) = KafkaTemplate(producerFactory)
}
