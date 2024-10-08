package com.music_service.domain.persistence.es.document

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document

@Document(indexName = "file")
@JsonIgnoreProperties(ignoreUnknown = true)
data class FileDocument(
    @Id
    val id: Long?,
    val musicId: Long,
    val fileType: String,
    val originalFileName: String,
    val savedName: String,
    val fileUrl: String,
    val createdAt: String,
    val updatedAt: String
)