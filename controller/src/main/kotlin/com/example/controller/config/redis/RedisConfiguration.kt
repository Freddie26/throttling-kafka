package com.example.controller.config.redis

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class RedisConfiguration {

    @Bean
    fun redisClient(props: RedisProperties): RedisClient {
        val redisUri = RedisURI.builder()
            .withSentinelMasterId(props.masterName)
            .apply { props.nodes.forEach { node -> this.withSentinel(node.host, node.port, props.password) } }
            .withPassword(props.password.toCharArray())
            .build()
        return RedisClient.create(redisUri)
    }

    @Bean
    fun redisProxyManager(client: RedisClient): LettuceBasedProxyManager<ByteArray> {
        // Храним информацию в хранилище 3 суток, потом токен будет удален
        return LettuceBasedProxyManager.builderFor(client)
            .withExpirationStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofDays(3)))
            .build()
    }
}
