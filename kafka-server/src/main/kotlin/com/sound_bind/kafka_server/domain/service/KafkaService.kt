package com.sound_bind.kafka_server.domain.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sound_bind.global.utils.logger
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@Service
class KafkaService(
    private val kafkaTemplate: KafkaTemplate<String, Map<String, Any>>,
) {
    private val mapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private val logger = logger()

    fun publishEvent(): (String) -> Unit = { event ->
        val data = if (event.trim().startsWith("[")) {
            mapper.readValue(event, List::class.java)
        } else {
            listOf(mapper.readValue(event, Map::class.java))
        }
        logger.info("Received Data : $data")

        val threads = data.size
        val executorService = Executors.newFixedThreadPool(threads)
        val latch = CountDownLatch(threads)

        for (d in data) {
            executorService.execute {
                val payload = mapper.readValue(mapper.writeValueAsString(d), Map::class.java)
                val topic = payload["topic"].toString()
                val message = payload["message"]

                val kafkaMessage = mapper.readValue(
                    mapper.writeValueAsString(message),
                    object: TypeReference<Map<String, Any>>() {}
                )
                kafkaTemplate.send(topic, kafkaMessage)
                latch.countDown()
            }
        }

        executorService.shutdown()
        latch.await()
    }
}