package com.sound_bind.global.dto

import java.util.UUID

data class OrchestrationRequestDTO(
    val id: UUID
)

data class OrchestrationResponseDTO(
    val id: UUID,
    val status: String
)