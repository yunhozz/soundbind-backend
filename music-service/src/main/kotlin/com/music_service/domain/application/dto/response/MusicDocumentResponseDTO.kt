package com.music_service.domain.application.dto.response

import com.music_service.domain.persistence.es.document.MusicDocument

data class MusicDocumentResponseDTO private constructor(
    val id: Long,
    val userId: Long,
    val userNickname: String,
    val title: String,
    val genres: Set<String>,
    val files: List<FileDetailsDTO>,
    val createdAt: String,
    val updatedAt: String
) {
    constructor(musicDocument: MusicDocument, files: List<FileDetailsDTO>): this(
        musicDocument.id!!,
        musicDocument.userId,
        musicDocument.userNickname,
        musicDocument.title,
        musicDocument.genres,
        files,
        musicDocument.createdAt,
        musicDocument.updatedAt
    )
}