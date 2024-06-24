package com.example.processor.service

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class BucketFactory(
    private val redisProxyManager: LettuceBasedProxyManager<ByteArray>,
) {

    fun createBucket(topic: String): Bucket {
        val configuration = createBucketConfiguration(topic)
        return redisProxyManager.builder().build(topic.toByteArray(), configuration)
    }

    private fun createBucketConfiguration(topic: String): BucketConfiguration {
        // Bandwidth не может иметь скорость 0, и не может генерировать больше 1 токена в наносекунду,
        // потому выставляем границы скоростей с помощью coerceIn
        return BucketConfiguration.builder()
            .addLimit(
                Bandwidth
                    .simple(1L, Duration.ofMinutes(1))
                    .withId("$topic-minute-rule")
            )
            .addLimit(
                Bandwidth
                    .simple(1L, Duration.ofSeconds(1))
                    .withId("$topic-second-rule")
            )
            .build()
    }
}
