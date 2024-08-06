package com.music_service.domain.application.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.web.multipart.MultipartFile

data class MusicCreateDTO(
    @field:NotBlank
    val title: String,
    @field:NotEmpty
    val genres: Set<String>,
    @field:NotNull
    val musicFile: MultipartFile,
    val imageFile: MultipartFile?
)
