package com.sound_bind.review_service.domain.application.dto.response

import com.sound_bind.review_service.domain.persistence.entity.Review
import java.time.LocalDateTime

data class ReviewDetailsDTO private constructor(
    val id: Long,
    val musicId: Long,
    val userId: Long,
    val userNickname: String,
    val userImageUrl: String?,
    val message: String,
    val score: Double,
    val commentNum: Int,
    val likes: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    constructor(review: Review) : this(
        review.id!!,
        review.musicId,
        review.userId,
        review.userNickname,
        review.userImageUrl,
        review.message,
        review.score,
        review.commentNum,
        review.likes,
        review.createdAt,
        review.updatedAt
    )
}