package com.music_service.domain.application.listener

import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import com.music_service.domain.persistence.entity.FileEntity
import com.music_service.domain.persistence.entity.Music

interface FileListener {
    fun onMusicUpload(fileInfoList: List<FileUploadResponseDTO>)
    fun onMusicUpdate(fileUrl: String?, dto: FileUploadResponseDTO?, music: Music, files: List<FileEntity>)
    fun onMusicDelete(musicId: Long, files: List<FileEntity>)
}