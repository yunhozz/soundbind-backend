package com.music_service.domain.application.manager.impl

import com.music_service.domain.application.dto.response.FileDownloadResponseDTO
import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import com.music_service.domain.application.file.FileHandler
import com.music_service.domain.application.file.FileHandlerFactory
import com.music_service.domain.application.file.ImageHandler
import com.music_service.domain.application.file.MusicHandler
import com.music_service.domain.application.manager.FileManager
import com.music_service.global.annotation.LogMessage
import com.music_service.global.config.AsyncConfig.Companion.THREAD_POOL_TASK_EXECUTOR
import jakarta.annotation.PostConstruct
import org.springframework.core.io.Resource
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class FileManagerImpl(private val fileHandlerFactory: FileHandlerFactory): FileManager {
    private lateinit var fileHandler: FileHandler
    private lateinit var musicHandler: MusicHandler
    private lateinit var imageHandler: ImageHandler

    @LogMessage("Generating File Information...")
    override fun generateFileInfo(file: MultipartFile): FileUploadResponseDTO =
        fileHandler.generateFileInfo(file)

    @Async(THREAD_POOL_TASK_EXECUTOR)
    @LogMessage("Uploading File...")
    override fun onMusicUpload(fileInfo: FileUploadResponseDTO) =
        fileHandler.upload(fileInfo.file, fileInfo.savedName)

    @Async(THREAD_POOL_TASK_EXECUTOR)
    @LogMessage("Updating File...")
    override fun onMusicUpdate(fileUrl: String?, dto: FileUploadResponseDTO?) {
        dto?.let {
            fileHandler.delete(fileUrl!!)
            fileHandler.upload(it.file, it.savedName)
        }
    }

    @Async(THREAD_POOL_TASK_EXECUTOR)
    @LogMessage("Deleting File...")
    override fun onMusicDelete(fileUrl: String) =
        fileHandler.delete(fileUrl)

    @LogMessage("Downloading Music File...")
    override fun downloadMusic(fileUrl: String): FileDownloadResponseDTO =
        musicHandler.downloadMusic(fileUrl)

    @LogMessage("Displaying Image File...")
    override fun displayImage(fileUrl: String): Resource =
        imageHandler.displayImage(fileUrl)

    @PostConstruct
    private fun init() {
        fileHandler = fileHandlerFactory.createFileHandler()
        musicHandler = fileHandlerFactory.createMusicHandler()
        imageHandler = fileHandlerFactory.createImageHandler()
    }
}