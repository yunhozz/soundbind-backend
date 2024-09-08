package com.music_service.domain.application.dto.response

import org.springframework.core.io.Resource

data class FileDownloadResponseDTO(
    val resource: Resource,
    val contentType: String
)