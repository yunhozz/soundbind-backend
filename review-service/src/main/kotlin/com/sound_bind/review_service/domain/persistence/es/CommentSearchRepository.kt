package com.sound_bind.review_service.domain.persistence.es

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface CommentSearchRepository: ElasticsearchRepository<CommentDocument, Long> {
    fun findByReviewIdOrderByCreatedAtAsc(reviewId: Long): List<CommentDocument>
}