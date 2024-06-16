package ru.sportmaster.speedlimiter.config.kafka

import com.example.processor.config.kafka.KafkaProperties
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class TopicProcessingConfig(
    private val kafkaProperties: KafkaProperties,
    @Value("\${bucket4j.tokens-limit-to-consume}") private val tokensLimitToConsume: Int
) {

    @PostConstruct
    fun init() {
    }
}
