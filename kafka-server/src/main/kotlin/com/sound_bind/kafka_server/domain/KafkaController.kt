package com.sound_bind.kafka_server.domain

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sound_bind.kafka_server.global.config.KafkaConfig.Companion.KAFKA_TEMPLATE
import com.sound_bind.kafka_server.global.config.KafkaConfig.Companion.TX_KAFKA_TEMPLATE
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ThreadLocalRandom

@RestController
@RequestMapping("/api/kafka")
class KafkaController(
    @Qualifier(KAFKA_TEMPLATE)
    private val kafkaTemplate: KafkaTemplate<String, Map<String, Any>>,
    @Qualifier(TX_KAFKA_TEMPLATE)
    private val kafkaTransactionTemplate: KafkaTemplate<String, Map<String, Any>>
) {

    companion object {
        private val mapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        private val log = LoggerFactory.getLogger(KafkaController::class.java)
    }

    @PostMapping
    fun sendKafkaMessage(@RequestBody data: String) {
        val response = if (data.trim().startsWith("[")) {
            mapper.readValue(data, List::class.java)
        } else {
            listOf(mapper.readValue(data, Map::class.java))
        }
        log.info("Received Data : $response")
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