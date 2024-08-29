package com.music_service.domain.application.dto.response

data class FileUploadResponseDTO(
    val originalFileName: String,
    val savedName: String,
    val fileUrl: String
)