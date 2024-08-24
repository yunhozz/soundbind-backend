package com.sound_bind.review_service.domain.persistence.repository.dto

import com.querydsl.core.annotations.QueryProjection
import com.sound_bind.review_service.domain.persistence.es.ReviewDocument
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ReviewQueryDTO @QueryProjection constructor(
    val id: Long,
    val userId: Long,
    val userNickname: String,
    val userImageUrl: String?,
    val message: String,
    val score: Double,
    val numberOfComments: Int,
    val likes: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    constructor(reviewDocument: ReviewDocument) : this(
        reviewDocument.id!!,
        reviewDocument.userId,
        reviewDocument.userNickname,
        reviewDocument.userImageUrl,
        reviewDocument.message,
        reviewDocument.score,
        reviewDocument.commentNum,
        reviewDocument.likes,
        convertStringToLocalDateTime(reviewDocument.createdAt),
        convertStringToLocalDateTime(reviewDocument.updatedAt)
    )

    var isLiked: Boolean? = null

    companion object {
        private fun convertStringToLocalDateTime(time: String): LocalDateTime {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
            return LocalDateTime.parse(time, formatter)
        }
    }
}