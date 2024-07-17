package com.sound_bind.review_service.global.dto.request

import java.time.LocalDateTime

data class ReviewCursorRequestDTO(
    val idCursor: Long? = null,
    val likesCursor: Int? = null,
    val createdAtCursor: LocalDateTime? = null
)
