package com.sound_bind.review_service.domain.application.manager.impl

import com.sound_bind.review_service.domain.application.ElasticsearchService
import com.sound_bind.review_service.domain.application.dto.response.ReviewDetailsDTO
import com.sound_bind.review_service.domain.application.manager.ReviewElasticsearchManager
import org.springframework.stereotype.Component

@Component
class ReviewElasticsearchManagerImpl(
    private val elasticsearchService: ElasticsearchService
): ReviewElasticsearchManager {

    override fun onReviewCreate(dto: ReviewDetailsDTO) =
        elasticsearchService.saveReviewInElasticSearch(dto)

    override fun onReviewDelete(reviewId: Long, commentIds: List<Long>) {
        elasticsearchService.deleteReviewInElasticSearch(reviewId)
        elasticsearchService.deleteCommentsInElasticSearch(commentIds)
    }

    override fun onReviewsDeleteByUserWithdraw(userId: Long) =
        elasticsearchService.deleteReviewsByUserIdInElasticSearch(userId)
}