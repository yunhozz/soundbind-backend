package com.music_service.domain.persistence.es.document

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document

@Document(indexName = "music")
data class MusicDocument(
    @Id
    val id: Long?,
    val userId: Long,
    val userNickname: String,
    val title: String,
    val genres: Set<String>,
    var likes: Int,
    var scoreAverage: Double,
    var isLiked: Boolean,
    val files: List<FileDocument>,
    val createdAt: String,
    val updatedAt: String
) {
    fun updateLikesAndScoreAverage(likes: Int, scoreAverage: Double) {
        this.likes = likes
        this.scoreAverage = scoreAverage
    }

    fun updateIsLiked(flag: Boolean) {
        this.isLiked = flag
    }
}