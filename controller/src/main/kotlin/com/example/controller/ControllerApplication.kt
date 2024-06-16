package com.example.controller

import com.example.controller.config.datasource.kafka.KafkaProperties
import com.example.controller.config.datasource.redis.RedisProperties
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
class ControllerApplication

fun main(args: Array<String>) {
    runApplication<ControllerApplication>(*args)
}
