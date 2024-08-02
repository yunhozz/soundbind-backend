package com.sound_bind.review_service.domain.application.dto.request

import jakarta.validation.constraints.NotBlank

data class CommentCreateDTO(
    @field:NotBlank
    val userNickname: String,
    @field:NotBlank
    val message: String
)
