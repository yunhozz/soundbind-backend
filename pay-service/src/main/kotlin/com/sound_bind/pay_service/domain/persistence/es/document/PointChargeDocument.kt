package com.sound_bind.pay_service.domain.persistence.es.document

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.elasticsearch.annotations.Document

@Document(indexName = "point_charge")
@JsonIgnoreProperties(ignoreUnknown = true)
data class PointChargeDocument(
    val id: Long,
    val userId: Long,
    val pointId: Long,
    val chargeType: String,
    val originalAmount: Int,
    val pointAmount: Int
)