package com.sound_bind.review_service.domain.persistence.es

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import java.time.LocalDateTime

@Document(indexName = "review")
data class ReviewDocument(
    @Id
    val id: Long?,
    val musicId: Long,
    val userId: Long,
    val userNickname: String,
    val userImageUrl: String?,
    val message: String,
    val score: Double,
    val commentNum: Int = 0,
    val likes: Int = 0,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)