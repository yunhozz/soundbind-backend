package com.sound_bind.review_service.domain.persistence.es

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface ReviewSearchRepository: ElasticsearchRepository<ReviewDocument, Long> {
    fun deleteAllByUserId(userId: Long)
}