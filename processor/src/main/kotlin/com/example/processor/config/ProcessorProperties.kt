package com.example.processor.config

import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties("processor")
data class ProcessorProperties(

    @field:Min(1)
    @field:Max(500)
    var tokensLimitToConsume: Int,

    @field:Min(1)
    @field:Max(25)
    var processorsPerTopic: Int,

    @field:NotEmpty
    var topics: Set<@Valid TopicPair>
)

data class TopicPair(

    @field:NotBlank
    var `in`: String,

    @field:NotBlank
    var out: String,
)
