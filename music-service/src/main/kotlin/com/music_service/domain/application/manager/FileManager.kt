package com.music_service.domain.application.manager

import com.music_service.domain.application.dto.response.FileDownloadResponseDTO
import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile

interface FileManager {
    fun generateFileInfo(file: MultipartFile): FileUploadResponseDTO
    fun downloadMusic(fileUrl: String): FileDownloadResponseDTO
    fun displayImage(fileUrl: String): Resource
}