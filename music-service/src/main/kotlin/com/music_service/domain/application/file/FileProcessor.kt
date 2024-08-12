package com.music_service.domain.application.file

import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile

sealed interface FileProcessor {
    fun upload(file: MultipartFile?): Triple<String, String, String>
    fun delete(fileUrl: String)
}

interface MusicFileProcessor: FileProcessor {
    fun download(fileUrl: String): Pair<Resource, String>
}

interface ImageFileProcessor: FileProcessor {
    fun update(fileUrl: String, file: MultipartFile?)
}