package com.sound_bind.kafka_server.domain.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sound_bind.global.utils.logger
import io.swagger.v3.oas.annotations.Operation
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@RestController
@RequestMapping("/api/kafka")
class KafkaController(
    private val kafkaTemplate: KafkaTemplate<String, Map<String, Any>>
) {

    private val logger = logger()
    private val mapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    @PostMapping
    @Operation(summary = "kafka topic 발행")
    fun sendKafkaMessage(@RequestBody data: String) {
        val response = if (data.trim().startsWith("[")) {
            mapper.readValue(data, List::class.java)
        } else {
            listOf(mapper.readValue(data, Map::class.java))
        }
        logger.info("Received Data : $response")
        val threads = response.size
        val executorService = Executors.newFixedThreadPool(threads)
        val latch = CountDownLatch(threads)

        executorService.execute {
            for (r in response) {
                val payload = mapper.readValue(mapper.writeValueAsString(r), Map::class.java)
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
    }
}