package com.sound_bind.review_service.global.dto.request

import jakarta.validation.constraints.NotBlank

data class CommentCreateDTO(
    @field:NotBlank
    val userNickname: String,
    @field:NotBlank
    val message: String
)
