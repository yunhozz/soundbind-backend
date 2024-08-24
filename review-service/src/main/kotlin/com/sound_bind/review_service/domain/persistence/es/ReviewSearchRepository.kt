package com.sound_bind.review_service.domain.persistence.es

import com.sound_bind.review_service.domain.persistence.repository.ReviewQueryRepository
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface ReviewSearchRepository: ElasticsearchRepository<ReviewDocument, Long>, ReviewQueryRepository {
    fun deleteAllByUserId(userId: Long)
}