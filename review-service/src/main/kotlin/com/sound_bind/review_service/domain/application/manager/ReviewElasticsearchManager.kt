package com.sound_bind.review_service.domain.application.manager

import com.sound_bind.review_service.domain.application.dto.response.ReviewDetailsDTO

interface ReviewElasticsearchManager {
    fun onReviewCreate(dto: ReviewDetailsDTO)
    fun onReviewDelete(reviewId: Long, commentIds: List<Long>)
    fun onReviewsDeleteByUserWithdraw(userId: Long)
}