package com.music_service.global.dto.request

import org.springframework.web.multipart.MultipartFile

data class MusicCreateDTO(
    val userId: Long,
    val title: String,
    val userNickname: String,
    val genres: Set<String>,
    val musicUrl: String,
    val imageUrl: String?,
    val musicFile: MultipartFile,
    val imageFile: MultipartFile?
)
