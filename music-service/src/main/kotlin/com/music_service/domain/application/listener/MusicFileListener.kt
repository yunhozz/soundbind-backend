package com.music_service.domain.application.listener

import com.music_service.domain.application.FileService
import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import org.springframework.stereotype.Component

@Component
class MusicFileListener(private val fileService: FileService): FileListener {

    override fun onMusicUpload(fileInfoList: List<FileUploadResponseDTO>) =
        fileService.upload(fileInfoList)

    override fun onMusicUpdate(fileUrl: String?, dto: FileUploadResponseDTO?) =
        fileService.update(fileUrl, dto)

    override fun onMusicDelete(fileUrls: List<String>) =
        fileService.delete(fileUrls)
}