package com.music_service.domain.application.listener

import com.music_service.domain.application.dto.response.FileDownloadResponseDTO
import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile

interface FileListener {
    fun generateFileInfo(file: MultipartFile): FileUploadResponseDTO
    fun onMusicUpload(fileInfoList: List<FileUploadResponseDTO>)
    fun onMusicUpdate(fileUrl: String?, dto: FileUploadResponseDTO?)
    fun onMusicDelete(fileUrls: List<String>)
    fun downloadMusic(fileUrl: String): FileDownloadResponseDTO
    fun displayImage(fileUrl: String): Resource
}