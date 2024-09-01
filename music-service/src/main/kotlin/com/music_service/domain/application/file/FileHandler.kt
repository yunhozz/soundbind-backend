package com.music_service.domain.application.file

import com.music_service.domain.application.dto.response.FileDownloadResponseDTO
import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import com.music_service.domain.application.file.FileVariable.CURRENT_DATE
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.util.UUID

interface MusicHandler {
    fun downloadMusic(fileUrl: String): FileDownloadResponseDTO
}

interface ImageHandler {
    fun displayImage(fileUrl: String): Resource
}

interface FileHandler: MusicHandler, ImageHandler {
    fun upload(file: MultipartFile, filename: String)
    fun delete(fileUrl: String)

    fun generateFileInfo(file: MultipartFile): FileUploadResponseDTO {
        val originalFilename = file.originalFilename
            ?: throw IOException("Original filename is null")
        val extension = originalFilename.substring(originalFilename.lastIndexOf("."))
        val uuid = UUID.randomUUID().toString()
        val savedName = "$uuid$extension"

        return FileUploadResponseDTO(
            file,
            originalFilename,
            savedName,
            fileUrl = "$CURRENT_DATE/$savedName"
        )
    }
}