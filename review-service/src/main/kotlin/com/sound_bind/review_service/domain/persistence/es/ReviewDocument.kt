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
    var commentNum: Int,
    var likes: Int,
    var isLiked: Boolean,
    val createdAt: String,
    val updatedAt: String
) {
    fun updateCommentNumAndLikes(commentNum: Int, likes: Int) {
        this.commentNum = commentNum
        this.likes = likes
    }

    fun updateIsLiked(flag: Boolean) {
        isLiked = flag
    }
}