package com.example.controller.service

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.TokensInheritanceStrategy
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class ChangeSpeedSettings(
    private val redisProxyManager: LettuceBasedProxyManager<ByteArray>,
) {

    fun modify(topic: String, tokensPerHour: Long) {
        val configuration = buildBucketConfiguration(topic, tokensPerHour)
        val bucket = redisProxyManager.builder().build(topic.toByteArray(), configuration)

        bucket.replaceConfiguration(configuration, TokensInheritanceStrategy.PROPORTIONALLY)
    }

    private fun buildBucketConfiguration(topic: String, tokensPerHour: Long): BucketConfiguration {
        // Скорость не может быть меньше или равна 0 (ограничение bucket4j)
        // Наложено ограничение на минимальную скорость - 3600 сообщений в час
        val minuteDuration = Duration.ofMinutes(1)
        val secondDuration = Duration.ofSeconds(1)
        return BucketConfiguration.builder()
            .addLimit(
                Bandwidth
                    .simple((tokensPerHour / 60).coerceIn(60, minuteDuration.toNanos()), minuteDuration)
                    .withId("$topic-minute-rule")
            )
            .addLimit(
                Bandwidth
                    .simple((tokensPerHour / 3600).coerceIn(1, secondDuration.toNanos()), secondDuration)
                    .withId("$topic-second-rule")
            )
            .build()
    }
}
