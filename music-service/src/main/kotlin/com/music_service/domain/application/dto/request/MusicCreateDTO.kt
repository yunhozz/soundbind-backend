package com.music_service.domain.application.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.web.multipart.MultipartFile

data class MusicCreateDTO(
    @field:NotBlank(message = "Please enter the title.")
    val title: String,
    @field:NotEmpty(message = "Please select at least one genre.")
    val genres: Set<String>,
    @field:NotNull(message = "Please upload the music file.")
    val musicFile: MultipartFile,
    val imageFile: MultipartFile?
)