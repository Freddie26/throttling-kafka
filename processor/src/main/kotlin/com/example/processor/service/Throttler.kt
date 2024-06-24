package com.example.processor.service

import io.github.bucket4j.Bucket
import org.apache.kafka.clients.consumer.ConsumerRecord

class Throttler(
    private val bucket: Bucket,
    private val tokensLimitToConsume: Int,
) {
    fun withThrottling(
        messages: List<ConsumerRecord<String, String>>,
        batchProcessor: (List<ConsumerRecord<String, String>>) -> Unit
    ) {
        var startIndex = 0
        while (startIndex < messages.size) {
            val recordsLeft = messages.size - startIndex
            val limit = recordsLeft.coerceAtMost(tokensLimitToConsume).toLong()
            val available = bucket.tryConsumeAsMuchAsPossible(limit).toInt()
            if (available > 0) {
                val batch = messages.subList(startIndex, (startIndex + available).coerceAtMost(messages.size))

                batchProcessor(batch)

                startIndex += available
            } else {
                // лимит пропускной способности исчерпан, не можем ничего отправить, нужно подождать
                Thread.sleep(100)
            }
        }
    }
}
