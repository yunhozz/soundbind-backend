package com.music_service.domain.application.dto.response

import org.springframework.web.multipart.MultipartFile

data class FileUploadResponseDTO(
    val file: MultipartFile,
    val originalFileName: String,
    val savedName: String,
    val fileUrl: String
)