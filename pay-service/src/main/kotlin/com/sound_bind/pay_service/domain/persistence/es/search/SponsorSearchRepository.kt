package com.sound_bind.pay_service.domain.persistence.es.search

import com.sound_bind.pay_service.domain.persistence.es.document.SponsorDocument
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface SponsorSearchRepository: ElasticsearchRepository<SponsorDocument, Long> {
    fun findAllByReceiverId(receiverId: Long): List<SponsorDocument>
}