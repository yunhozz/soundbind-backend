package com.sound_bind.pay_service.domain.persistence.es.document

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.elasticsearch.annotations.Document

@Document(indexName = "sponsor")
@JsonIgnoreProperties(ignoreUnknown = true)
data class SponsorDocument(
    val id: Long,
    val senderId: Long,
    val receiverId: Long,
    val pointId: Long,
    val pointAmount: Int,
    var isCompleted: Boolean,
    val createdAt: String
) {
    fun receive() {
        isCompleted = true
    }
}