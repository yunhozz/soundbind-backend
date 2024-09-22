package com.music_service.domain.application.dto.request

import org.springframework.web.multipart.MultipartFile

data class MusicUpdateDTO(
    val title: String,
    val genres: Set<String>,
    val imageFile: MultipartFile?
)