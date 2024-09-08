package com.sound_bind.review_service.domain.persistence.es

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.elasticsearch.annotations.Document

@Document(indexName = "comment")
@JsonIgnoreProperties(ignoreUnknown = true)
data class CommentDocument(
    val id: Long?,
    val reviewId: Long,
    val userId: Long,
    val userNickname: String,
    val message: String,
    val createdAt: String,
    val updatedAt: String
)
