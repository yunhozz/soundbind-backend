package com.music_service.domain.application.file

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

    override fun downloadMusic() {
//        musicResourceHandler.download()
    }

    override fun displayImage() {
//        imageResourceHandler.display()
    }

    override fun deleteMusic(fileUrl: String) = musicResourceHandler.deleteMusic(fileUrl)

    override fun deleteImage(fileUrl: String) = imageResourceHandler.deleteImage(fileUrl)
}