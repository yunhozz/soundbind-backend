package com.sound_bind.review_service.domain.application.listener

import com.sound_bind.review_service.domain.application.dto.response.ReviewDetailsDTO

interface ReviewElasticsearchListener {
    fun onReviewCreate(dto: ReviewDetailsDTO)
    fun onReviewDelete(reviewId: Long, commentIds: List<Long>)
    fun onReviewsDeleteByUserWithdraw(userId: Long)
}