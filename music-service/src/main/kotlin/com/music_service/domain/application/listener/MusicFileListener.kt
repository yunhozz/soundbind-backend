package com.music_service.domain.application.listener

import com.music_service.domain.application.FileService
import com.music_service.domain.application.dto.response.FileDownloadResponseDTO
import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class MusicFileListener(private val fileService: FileService): FileListener {

    override fun generateFileInfo(file: MultipartFile): FileUploadResponseDTO =
        fileService.generateFileInfo(file)

    override fun onMusicUpload(fileInfoList: List<FileUploadResponseDTO>) =
        fileInfoList.forEach { fileService.upload(it) }

    override fun onMusicUpdate(fileUrl: String?, dto: FileUploadResponseDTO?) =
        fileService.update(fileUrl, dto)

    override fun onMusicDelete(fileUrls: List<String>) =
        fileUrls.forEach { fileService.delete(it) }

    override fun downloadMusic(fileUrl: String): FileDownloadResponseDTO =
        fileService.downloadMusic(fileUrl)

    override fun displayImage(fileUrl: String): Resource =
        fileService.displayImage(fileUrl)
}