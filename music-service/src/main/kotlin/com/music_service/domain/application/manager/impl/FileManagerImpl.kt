package com.music_service.domain.application.manager.impl

import com.music_service.domain.application.dto.response.FileDownloadResponseDTO
import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import com.music_service.domain.application.file.FileHandler
import com.music_service.domain.application.file.FileHandlerFactory
import com.music_service.domain.application.file.ImageHandler
import com.music_service.domain.application.file.MusicHandler
import com.music_service.domain.application.manager.FileManager
import com.music_service.global.config.AsyncConfig.Companion.THREAD_POOL_TASK_EXECUTOR
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class FileManagerImpl(private val fileHandlerFactory: FileHandlerFactory): FileManager {

    private val log: Logger = LoggerFactory.getLogger(FileManagerImpl::class.java)

    private lateinit var fileHandler: FileHandler
    private lateinit var musicHandler: MusicHandler
    private lateinit var imageHandler: ImageHandler

    // TODO: AOP 이용하여 로그 기능 추가

    override fun generateFileInfo(file: MultipartFile): FileUploadResponseDTO =
        fileHandler.generateFileInfo(file)

    @Async(THREAD_POOL_TASK_EXECUTOR)
    override fun onMusicUpload(fileInfo: FileUploadResponseDTO) {
        log.info("=====File Upload Start=====")
        fileHandler.upload(fileInfo.file, fileInfo.savedName)
        log.info("=====File Upload End=====")
    }

    @Async(THREAD_POOL_TASK_EXECUTOR)
    override fun onMusicUpdate(fileUrl: String?, dto: FileUploadResponseDTO?) {
        log.info("=====File Update Start=====")
        dto?.let {
            fileHandler.delete(fileUrl!!)
            fileHandler.upload(it.file, it.savedName)
        }
        log.info("=====File Update End=====")
    }

    @Async(THREAD_POOL_TASK_EXECUTOR)
    override fun onMusicDelete(fileUrl: String) {
        log.info("=====File Delete Start=====")
        fileHandler.delete(fileUrl)
        log.info("=====File Delete End=====")
    }

    override fun downloadMusic(fileUrl: String): FileDownloadResponseDTO =
        musicHandler.downloadMusic(fileUrl)

    override fun displayImage(fileUrl: String): Resource =
        imageHandler.displayImage(fileUrl)

    @PostConstruct
    private fun init() {
        fileHandler = fileHandlerFactory.createFileHandler()
        musicHandler = fileHandlerFactory.createMusicHandler()
        imageHandler = fileHandlerFactory.createImageHandler()
    }
}