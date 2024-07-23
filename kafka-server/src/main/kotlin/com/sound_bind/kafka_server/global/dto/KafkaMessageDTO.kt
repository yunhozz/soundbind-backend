package com.sound_bind.kafka_server.global.dto

data class KafkaMessageDTO(
    val topic: String,
    val message: Map<String, String>
)