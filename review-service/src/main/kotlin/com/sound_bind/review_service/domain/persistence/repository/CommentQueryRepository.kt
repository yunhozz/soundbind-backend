package com.sound_bind.review_service.domain.persistence.repository

import com.sound_bind.review_service.domain.persistence.repository.dto.CommentQueryDTO

interface CommentQueryRepository {

    fun findCommentsByReviewId(reviewId: Long): List<CommentQueryDTO>
}