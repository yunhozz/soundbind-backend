package com.music_service.domain.application

import com.music_service.global.handler.file.FileHandler
import com.music_service.global.handler.file.ImageResourceHandler
import com.music_service.global.handler.file.MusicResourceHandler
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FileHandlerImpl(
    private val musicResourceHandler: MusicResourceHandler,
    private val imageResourceHandler: ImageResourceHandler
): FileHandler {
    override fun uploadMusic(file: MultipartFile) {
        musicResourceHandler.upload(file)
    }

    override fun uploadImage(file: MultipartFile) {
        imageResourceHandler.upload(file)
    }

    override fun downloadMusic() {
//        musicResourceHandler.download()
    }

    override fun displayImage() {
//        imageResourceHandler.display()
    }
}