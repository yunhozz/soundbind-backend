package com.sound_bind.global.dto

data class OrchestrationRequestDTO(
    val id: String,
    val event: KafkaEvent
)

data class OrchestrationResponseDTO(
    val id: String,
    val status: String
)