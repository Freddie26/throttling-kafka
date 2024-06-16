package com.example.controller.config.datasource.kafka

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "kafka")
data class KafkaProperties(

    @field:NotBlank
    var bootstrapServers: String? = null,

    @field:Min(1)
    @field:Max(5000)
    var maxPollRecords: Int = 50,

    @field:NotBlank
    val isolationLevel: String = "read_committed",

    /**
     * Кол-во потоков на каждый канал, которые обрабатывают правило.
     */
    @field:Min(1)
    @field:Max(100)
    var threadsPerTopic: Int = 2,
)
