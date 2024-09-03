package com.music_service.domain.application

import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import com.music_service.domain.application.file.FileHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class FileService(private val fileHandler: FileHandler) {

    // TODO: 예외 보상 처리

    private val log: Logger = LoggerFactory.getLogger(FileService::class.java)

    @Async
    fun upload(fileInfoList: List<FileUploadResponseDTO>) {
        log.info("=====File Upload Start=====")
        fileInfoList.forEach { info -> fileHandler.upload(info.file, info.savedName) }
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
    fun delete(fileUrls: List<String>) {
        log.info("=====File Delete Start=====")
        fileUrls.forEach { fileHandler.delete(it) }
        log.info("=====File Delete End=====")
    }
}