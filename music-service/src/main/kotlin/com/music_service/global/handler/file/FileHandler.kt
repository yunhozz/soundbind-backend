package com.music_service.global.handler.file

import org.springframework.web.multipart.MultipartFile

interface FileHandler {
    fun uploadMusic(file: MultipartFile): Triple<String, String, String>
    fun uploadImage(file: MultipartFile): Triple<String, String, String>
    fun downloadMusic()
    fun displayImage()
}