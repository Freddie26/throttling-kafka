package com.example.processor.service

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import org.springframework.stereotype.Component

@Component
class ThrottledMessagesRegistry(
    private val meterRegistry: MeterRegistry,
) {

    private val counters = mutableMapOf<String, Counter>()

    fun increment(topic: String, count: Int) {
        getCounter(topic).increment(count.toDouble())
    }

    private fun getCounter(topic: String): Counter {
        return counters.computeIfAbsent(topic) { meterRegistry.counter(COUNTER_NAME, emptySet<Tag>()) }
    }

    companion object {
        private const val COUNTER_NAME = "throttled.messages.counter"
    }
}
