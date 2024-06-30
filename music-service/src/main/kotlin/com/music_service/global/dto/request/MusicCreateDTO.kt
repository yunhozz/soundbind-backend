package com.music_service.global.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.web.multipart.MultipartFile

data class MusicCreateDTO(
    @field:NotNull
    val userId: Long,
    @field:NotBlank
    val title: String,
    @field:NotBlank
    val userNickname: String,
    @field:NotEmpty
    val genres: Set<String>,
    @field:NotNull
    val musicFile: MultipartFile,
    val imageFile: MultipartFile?
)
