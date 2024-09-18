package com.music_service.domain.application.file

import com.music_service.domain.application.dto.response.FileDownloadResponseDTO
import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile

interface FileHandler {
    fun generateFileInfo(file: MultipartFile): FileUploadResponseDTO
    fun upload(file: MultipartFile, filename: String)
    fun delete(fileUrl: String)
}

interface MusicHandler {
    fun downloadMusic(fileUrl: String): FileDownloadResponseDTO
}

interface ImageHandler {
    fun displayImage(fileUrl: String): Resource
}