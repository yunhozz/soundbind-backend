package com.sound_bind.review_service.domain.application.dto.response

import com.sound_bind.review_service.domain.persistence.entity.Comment
import com.sound_bind.review_service.domain.persistence.entity.Review
import java.time.LocalDateTime

data class CommentDetailsDTO private constructor(
    val id: Long,
    val userId: Long,
    val reviewId: Long,
    val reviewerId: Long,
    val userNickname: String,
    val message: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    constructor(comment: Comment, review: Review): this(
        comment.id!!,
        comment.userId,
        review.id!!,
        review.userId,
        comment.userNickname,
        comment.message,
        comment.createdAt,
        comment.updatedAt
    )
}
