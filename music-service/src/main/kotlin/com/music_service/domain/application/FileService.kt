package com.music_service.domain.application

import com.music_service.domain.application.dto.response.FileDownloadResponseDTO
import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import com.music_service.domain.application.file.FileHandler
import com.music_service.domain.application.file.FileHandlerFactory
import com.music_service.domain.application.file.ImageHandler
import com.music_service.domain.application.file.MusicHandler
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FileService(private val fileHandlerFactory: FileHandlerFactory) {

    // TODO: 예외 보상 처리

    private val log: Logger = LoggerFactory.getLogger(FileService::class.java)

    private lateinit var fileHandler: FileHandler
    private lateinit var musicHandler: MusicHandler
    private lateinit var imageHandler: ImageHandler

    fun generateFileInfo(file: MultipartFile): FileUploadResponseDTO =
        fileHandler.generateFileInfo(file)

    @Async
    fun upload(fileInfo: FileUploadResponseDTO) {
        log.info("=====File Upload Start=====")
        fileHandler.upload(fileInfo.file, fileInfo.savedName)
        log.info("=====File Upload End=====")
    }

    @Async
    fun update(fileUrl: String?, dto: FileUploadResponseDTO?) {
        log.info("=====File Update Start=====")
        dto?.let {
            fileHandler.delete(fileUrl!!)
            fileHandler.upload(it.file, it.savedName)
        }
        log.info("=====File Update End=====")
    }

    @Async
    fun delete(fileUrl: String) {
        log.info("=====File Delete Start=====")
        fileHandler.delete(fileUrl)
        log.info("=====File Delete End=====")
    }

    fun downloadMusic(fileUrl: String): FileDownloadResponseDTO =
        musicHandler.downloadMusic(fileUrl)

    fun displayImage(fileUrl: String): Resource =
        imageHandler.displayImage(fileUrl)

    @PostConstruct
    private fun init() {
        fileHandler = fileHandlerFactory.createFileHandler()
        musicHandler = fileHandlerFactory.createMusicHandler()
        imageHandler = fileHandlerFactory.createImageHandler()
    }
}