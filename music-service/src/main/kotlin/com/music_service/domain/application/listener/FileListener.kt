package com.music_service.domain.application.listener

import com.music_service.domain.application.dto.response.FileUploadResponseDTO

interface FileListener {
    fun onMusicUpload(fileInfoList: List<FileUploadResponseDTO>)
    fun onMusicUpdate(fileUrl: String?, dto: FileUploadResponseDTO?)
    fun onMusicDelete(fileUrls: List<String>)
}