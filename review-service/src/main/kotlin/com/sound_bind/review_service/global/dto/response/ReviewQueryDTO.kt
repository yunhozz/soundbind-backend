package com.sound_bind.review_service.global.dto.response

import com.querydsl.core.annotations.QueryProjection
import java.time.LocalDateTime

data class ReviewQueryDTO @QueryProjection constructor(
    val id: Long,
    val userId: Long,
    val userNickname: String,
    val userImageUrl: String,
    val message: String,
    val score: Double,
    val likes: Int,
    val countOfComments: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
