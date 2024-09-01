package com.music_service.domain.application.file

import com.music_service.domain.application.dto.response.FileDownloadResponseDTO
import org.springframework.core.io.Resource

interface MusicHandler {
    fun downloadMusic(fileUrl: String): FileDownloadResponseDTO
}

interface ImageHandler {
    fun displayImage(fileUrl: String): Resource
}

interface FileHandlerFactory: MusicHandler, ImageHandler {
    fun createMusicHandler(): MusicHandler
    fun createImageHandler(): ImageHandler
}