package com.music_service.global.dto.request

import org.springframework.web.multipart.MultipartFile

data class MusicUpdateDTO(
    val title: String,
    val genres: List<String>,
    val imageFile: MultipartFile
)
