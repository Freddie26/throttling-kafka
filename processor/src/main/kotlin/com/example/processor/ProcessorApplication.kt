package com.example.processor

import com.example.processor.config.kafka.KafkaProperties
import com.example.processor.config.redis.RedisProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties(
    value = [
        KafkaProperties::class,
        RedisProperties::class,
    ]
)
@SpringBootApplication
class ProcessorApplication

fun main(args: Array<String>) {
    runApplication<ProcessorApplication>(*args)
}
