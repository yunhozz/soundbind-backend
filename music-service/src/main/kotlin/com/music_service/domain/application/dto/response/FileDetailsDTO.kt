package com.music_service.domain.application.dto.response

import com.music_service.domain.persistence.entity.FileEntity
import com.music_service.domain.persistence.es.document.FileDocument
import com.music_service.global.util.DateTimeUtils
import java.time.LocalDateTime

data class FileDetailsDTO private constructor(
    val id: Long?,
    val musicId: Long,
    val fileType: String,
    val originalFileName: String,
    val savedName: String,
    val fileUrl: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    constructor(file: FileEntity, musicId: Long): this(
        file.id,
        musicId,
        file.fileType.toString(),
        file.originalFileName,
        file.savedName,
        file.fileUrl,
        file.createdAt,
        file.updatedAt
    )

    constructor(fileDocument: FileDocument, musicId: Long): this(
        fileDocument.id,
        musicId,
        fileDocument.fileType,
        fileDocument.originalFileName,
        fileDocument.savedName,
        fileDocument.fileUrl,
        DateTimeUtils.convertStringToLocalDateTime(fileDocument.createdAt),
        DateTimeUtils.convertStringToLocalDateTime(fileDocument.updatedAt)
    )
}