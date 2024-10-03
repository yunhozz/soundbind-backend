package com.sound_bind.pay_service.domain.persistence.es.search

import com.sound_bind.pay_service.domain.persistence.es.document.PointChargeDocument
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface PointChargeSearchRepository: ElasticsearchRepository<PointChargeDocument, Long> {
    fun findAllByUserId(userId: Long): List<PointChargeDocument>
}