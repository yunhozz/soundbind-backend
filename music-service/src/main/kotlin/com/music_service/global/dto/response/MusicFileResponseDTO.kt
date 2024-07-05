package com.music_service.global.dto.response

import org.springframework.core.io.Resource

data class MusicFileResponseDTO(
    val musicFile: Resource,
    val contentType: String,
    val fileName: String
)
