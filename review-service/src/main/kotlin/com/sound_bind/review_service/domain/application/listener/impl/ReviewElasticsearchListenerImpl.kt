package com.sound_bind.review_service.domain.application.listener.impl

import com.sound_bind.review_service.domain.application.ElasticsearchService
import com.sound_bind.review_service.domain.application.dto.response.ReviewDetailsDTO
import com.sound_bind.review_service.domain.application.listener.ReviewElasticsearchListener
import org.springframework.stereotype.Component

@Component
class ReviewElasticsearchListenerImpl(
    private val elasticsearchService: ElasticsearchService
): ReviewElasticsearchListener {

    override fun onReviewCreate(dto: ReviewDetailsDTO) =
        elasticsearchService.saveReviewInElasticSearch(dto)

    override fun onReviewDelete(reviewId: Long, commentIds: List<Long>) {
        elasticsearchService.deleteReviewInElasticSearch(reviewId)
        elasticsearchService.deleteCommentsInElasticSearch(commentIds)
    }

    override fun onReviewsDeleteByUserWithdraw(userId: Long) =
        elasticsearchService.deleteReviewsByUserIdInElasticSearch(userId)
}