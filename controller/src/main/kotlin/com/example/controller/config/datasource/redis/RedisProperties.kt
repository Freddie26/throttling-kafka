package com.example.controller.config.datasource.redis

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(prefix = "datasource.redis")
data class RedisProperties(
    @field:NotBlank
    var masterName: String = "",

    @field:NotBlank
    var password: String = "",

    @field:Valid
    @field:NotEmpty
    var nodes: List<RedisNodeConfig> = listOf(),
)

data class RedisNodeConfig(
    @field:NotBlank var host: String = "",
    @field:Min(1) var port: Int = 0,
)
