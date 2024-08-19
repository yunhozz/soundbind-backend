package com.sound_bind.review_service.domain.application.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class ReviewUpdateDTO(
    @field:NotBlank
    val message: String,
    @field:NotNull
    val score: Double
)
