package com.sound_bind.kafka_server.domain

import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/kafka")
class KafkaController(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {

    @PostMapping("/produce")
    fun produceTest(@RequestBody dto: ProduceRequestDTO) {
        val record = ProducerRecord<String, Any>(dto.topic, dto.message)
        kafkaTemplate.send(record)
    }
}