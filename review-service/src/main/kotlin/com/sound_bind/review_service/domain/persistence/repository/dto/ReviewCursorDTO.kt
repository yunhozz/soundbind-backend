package com.sound_bind.review_service.domain.persistence.repository.dto

data class ReviewCursorDTO(
    val idCursor: Long? = null,
    val likesCursor: Int? = null,
    val createdAtCursor: String? = null
)
