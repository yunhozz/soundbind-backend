package com.music_service.domain.persistence.es

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document

@Document(indexName = "file")
data class FileDocument(
    @Id
    val id: Long?,
    val musicId: Long,
    val fileType: String,
    val originalFileName: String,
    val savedName: String,
    val fileUrl: String
)