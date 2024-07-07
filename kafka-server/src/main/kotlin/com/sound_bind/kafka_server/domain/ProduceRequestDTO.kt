package com.sound_bind.kafka_server.domain

data class ProduceRequestDTO(
    val topic: String,
    val message: Any
)
