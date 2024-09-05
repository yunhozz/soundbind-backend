package com.music_service.domain.application.dto.response

import com.music_service.domain.persistence.entity.FileType
import com.music_service.domain.persistence.es.document.MusicDocument

data class MusicSimpleResponseDTO(
    val id: Long,
    val userNickname: String,
    val title: String,
    val imageUrl: String?,
    val likes: Int
) {
    constructor(musicDocument: MusicDocument?): this(
        musicDocument?.id!!,
        musicDocument.userNickname,
        musicDocument.title,
        musicDocument.files
            .firstOrNull { f -> f.fileType == FileType.IMAGE.name }
            ?.fileUrl,
        musicDocument.likes
    )
}