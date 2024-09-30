package com.music_service.domain.application.manager

import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import com.music_service.domain.application.dto.response.MusicDetailsDTO

interface AsyncManager {
    // file
    fun musicUploadWithAsync(fileInfo: FileUploadResponseDTO)
    fun musicUpdateWithAsync(fileUrl: String, dto: FileUploadResponseDTO)
    fun musicDeleteWithAsync(fileUrl: String)

    // elasticsearch
    fun saveMusicByElasticsearchWithAsync(dto: MusicDetailsDTO)
    fun deleteMusicByElasticsearchWithAsync(musicId: Long, fileIds: List<Long>)
}