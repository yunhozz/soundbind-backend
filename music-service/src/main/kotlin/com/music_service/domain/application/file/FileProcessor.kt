package com.music_service.domain.application.file

import com.music_service.domain.application.dto.response.FileDownloadResponseDTO
import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import org.springframework.web.multipart.MultipartFile

interface FileProcessor {
    fun upload(file: MultipartFile?): FileUploadResponseDTO
    fun delete(fileUrl: String)
}

interface MusicProcessor: FileProcessor {
    fun download(fileUrl: String): FileDownloadResponseDTO
}

interface ImageProcessor: FileProcessor {
    fun update(fileUrl: String, file: MultipartFile?): FileUploadResponseDTO
    fun display(fileUrl: String)
}