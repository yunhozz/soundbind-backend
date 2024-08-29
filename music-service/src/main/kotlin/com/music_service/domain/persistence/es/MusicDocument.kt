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
    val files: List<FileDocument>
)