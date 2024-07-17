package com.sound_bind.review_service.global.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class ReviewCreateDTO(
    @field:NotBlank
    val userNickname: String,
    val userImageUrl: String,
    @field:NotBlank
    val message: String,
    @field:NotNull
    val score: Double
)
