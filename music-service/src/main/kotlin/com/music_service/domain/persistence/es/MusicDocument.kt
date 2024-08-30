package com.music_service.domain.persistence.es

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
    val likes: Int,
    val scoreAverage: Double,
    val files: List<FileDocument>,
    val createdAt: String,
    val updatedAt: String
)