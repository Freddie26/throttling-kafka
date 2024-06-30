package com.example.processor.service

import com.example.processor.config.ProcessorProperties
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.scheduling.concurrent.CustomizableThreadFactory
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
class ProcessingRunner(
    private val processorProperties: ProcessorProperties,
    private val processorFactory: ProcessorFactory,
) {

    private val tasks = mutableListOf<TopicProcessor>()
    private val threadPool = Executors.newFixedThreadPool(
        processorProperties.topics.size * processorProperties.processorsPerTopic,
        CustomizableThreadFactory("processor-")
    )

    @PostConstruct
    fun inti() {
        processorProperties.topics.forEach { item ->
            processorFactory.create(item.`in`, item.out)
                .forEach {
                    tasks.add(it)
                    threadPool.execute(it)
                }
        }
    }

    @PreDestroy
    fun destroy() {
        tasks.forEach { it.stop() }

        threadPool.shutdown()
        threadPool.awaitTermination(30, TimeUnit.SECONDS)
    }
}
