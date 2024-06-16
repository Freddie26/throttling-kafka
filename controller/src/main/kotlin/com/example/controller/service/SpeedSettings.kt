package com.example.controller.service

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.TokensInheritanceStrategy
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class SpeedSettings(
    private val redisProxyManager: LettuceBasedProxyManager<ByteArray>,
) {

    fun modify(topic: String, speed: Long) {
        val key = topic.toByteArray()
        val configuration = buildBucketConfiguration(topic, speed)
        val bucket = redisProxyManager.builder().build(key, configuration)

        bucket.replaceConfiguration(configuration, TokensInheritanceStrategy.PROPORTIONALLY)
    }

    private fun buildBucketConfiguration(topic: String, speed: Long): BucketConfiguration =
        if (speed <= 0L) {
            BucketConfiguration.builder()
                // скорость не может быть 0
                .addLimit(Bandwidth.simple(1, Duration.ofNanos(Long.MAX_VALUE)).withId("$topic-minute-rule"))
                .build()
        } else {
            val minuteDuration = Duration.ofMinutes(1)

            BucketConfiguration.builder()
                .addLimit(
                    Bandwidth
                        // If speed is less than 60, then calculated value will be 0, which it is not correct value for capacity.
                        .simple((speed / 60).coerceIn(1, minuteDuration.toNanos()), minuteDuration)
                        .withId("$topic-minute-rule")
                )
                .build()
        }
}
