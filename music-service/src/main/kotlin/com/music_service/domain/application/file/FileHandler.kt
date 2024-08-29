package com.music_service.domain.application.file

import com.music_service.domain.application.dto.response.FileDownloadResponseDTO
import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

abstract class FileHandler<T: FileProcessor>(protected val processor: T)

@Component
class MusicHandler: FileHandler<MusicProcessor>(MusicProcessorImpl()) {
    private val log = LoggerFactory.getLogger(MusicHandler::class.java)

    fun uploadMusic(file: MultipartFile?): FileUploadResponseDTO {
        log.debug("Uploading music to : ${file?.originalFilename}")
        return processor.upload(file)
    }

    fun downloadMusic(fileUrl: String): FileDownloadResponseDTO {
        log.debug("Downloading music from $fileUrl")
        return processor.download(fileUrl)
    }

    fun deleteMusic(fileUrl: String) {
        log.debug("Deleting music from $fileUrl")
        processor.delete(fileUrl)
    }
}

@Component
class ImageHandler: FileHandler<ImageProcessor>(ImageProcessorImpl()) {
    private val log = LoggerFactory.getLogger(ImageHandler::class.java)

    fun uploadImage(file: MultipartFile?): FileUploadResponseDTO {
        log.debug("Uploading image to ${file?.originalFilename}")
        return processor.upload(file)
    }

    fun displayImage(fileUrl: String) {
        log.debug("Displaying image from $fileUrl")
    }

    fun updateImage(fileUrl: String, file: MultipartFile?): FileUploadResponseDTO {
        log.debug("Updating image from $fileUrl")
        return processor.update(fileUrl, file)
    }

    fun deleteImage(fileUrl: String) {
        log.debug("Deleting image from $fileUrl")
        processor.delete(fileUrl)
    }
}