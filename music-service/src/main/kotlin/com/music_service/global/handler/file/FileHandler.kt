package com.music_service.global.handler.file

import org.springframework.web.multipart.MultipartFile

interface FileHandler {
    fun uploadMusic(file: MultipartFile)
    fun uploadImage(file: MultipartFile)
    fun downloadMusic()
    fun displayImage()
}