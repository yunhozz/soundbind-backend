package com.sound_bind.kafka_server.domain

import com.sound_bind.kafka_server.global.dto.KafkaMessageDTO
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/kafka")
class KafkaController(private val kafkaTemplate: KafkaTemplate<String, Map<String, String>>) {

    companion object {
        private val log = LoggerFactory.getLogger(KafkaController::class.java)
    }

    @PostMapping
    fun sendKafkaMessage(@RequestBody dto: KafkaMessageDTO) {
        log.info("topic = ${dto.topic}")
        log.info("message = ${dto.message}")
        kafkaTemplate.send(dto.topic, dto.message)
    }
}