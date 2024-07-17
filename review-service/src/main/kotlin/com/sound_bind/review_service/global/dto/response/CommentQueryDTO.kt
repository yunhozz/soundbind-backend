package com.sound_bind.review_service.global.dto.response

import com.querydsl.core.annotations.QueryProjection
import java.time.LocalDateTime

data class CommentQueryDTO @QueryProjection constructor(
    val userId: Long,
    val userNickname: String,
    val message: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
