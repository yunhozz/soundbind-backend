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
    val numberOfComments: Int,
    val likes: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    var isLiked: Boolean? = null
}
