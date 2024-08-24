package com.sound_bind.review_service.domain.persistence.es

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.elasticsearch.annotations.Document

@Document(indexName = "review")
@JsonIgnoreProperties(ignoreUnknown = true)
data class ReviewDocument(
    val id: Long?,
    val musicId: Long,
    val userId: Long,
    val userNickname: String,
    val userImageUrl: String?,
    val message: String,
    val score: Double,
    val commentNum: Int = 0,
    val likes: Int = 0,
    val createdAt: String,
    val updatedAt: String
)