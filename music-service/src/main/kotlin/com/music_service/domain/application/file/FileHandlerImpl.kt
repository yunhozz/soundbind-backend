package com.music_service.domain.application.file

import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FileHandlerImpl(
    private val musicResourceHandler: MusicResourceHandler,
    private val imageResourceHandler: ImageResourceHandler
): FileHandler {

    override fun uploadMusic(file: MultipartFile): Triple<String, String, String>
        = musicResourceHandler.uploadMusic(file)

    override fun uploadImage(file: MultipartFile): Triple<String, String, String>
        = imageResourceHandler.uploadImage(file)

    override fun updateImage(fileUrl: String, file: MultipartFile) {
        deleteImage(fileUrl)
        uploadImage(file)
    }

    override fun downloadMusic(fileUrl: String): Pair<Resource, String>
        = musicResourceHandler.downloadMusic(fileUrl)

    override fun deleteMusic(fileUrl: String) = musicResourceHandler.deleteMusic(fileUrl)

    override fun deleteImage(fileUrl: String) = imageResourceHandler.deleteImage(fileUrl)
}