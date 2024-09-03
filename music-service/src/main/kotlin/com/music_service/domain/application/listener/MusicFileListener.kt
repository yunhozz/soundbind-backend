package com.music_service.domain.application.listener

import com.music_service.domain.application.FileService
import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import com.music_service.domain.persistence.entity.FileEntity
import com.music_service.domain.persistence.entity.Music
import org.springframework.stereotype.Component

@Component
class MusicFileListener(private val fileService: FileService): FileListener {

    override fun onMusicUpload(fileInfoList: List<FileUploadResponseDTO>) =
        fileService.upload(fileInfoList)

    override fun onMusicUpdate(fileUrl: String?, dto: FileUploadResponseDTO?, music: Music, files: List<FileEntity>) =
        fileService.update(fileUrl, dto, music, files)

    override fun onMusicDelete(musicId: Long, files: List<FileEntity>) =
        fileService.delete(musicId, files)
}