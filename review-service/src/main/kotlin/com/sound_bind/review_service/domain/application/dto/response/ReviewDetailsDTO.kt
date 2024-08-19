package com.sound_bind.review_service.domain.application.dto.response

import com.sound_bind.review_service.domain.persistence.entity.Review

data class ReviewDetailsDTO private constructor(
    val id: Long,
    val musicId: Long,
    val userId: Long,
    val userNickname: String,
    val userImageUrl: String?,
    val message: String,
    val score: Double,
    val likes: Int
) {
    constructor(review: Review) : this(
        id = review.id!!,
        musicId = review.musicId,
        userId = review.userId,
        userNickname = review.userNickname,
        userImageUrl = review.userImageUrl,
        message = review.message,
        score = review.score,
        likes = review.likes
    )
}