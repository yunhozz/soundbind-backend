package com.music_service.domain.application.file

import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile

interface FileHandler {
    fun uploadMusic(file: MultipartFile): Triple<String, String, String>
    fun uploadImage(file: MultipartFile): Triple<String, String, String>
    fun updateImage(fileUrl: String, file: MultipartFile)
    fun downloadMusic(fileUrl: String): Pair<Resource, String>
    fun deleteMusic(fileUrl: String)
    fun deleteImage(fileUrl: String)
}