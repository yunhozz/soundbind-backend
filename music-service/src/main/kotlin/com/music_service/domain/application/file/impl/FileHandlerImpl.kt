package com.music_service.domain.application.file.impl

import com.music_service.domain.application.dto.response.FileDownloadResponseDTO
import com.music_service.domain.application.file.FileHandler
import com.music_service.domain.application.file.FileVariable.ABSOLUTE_PATH
import com.music_service.domain.application.file.FileVariable.FILE_DIRECTORY
import com.music_service.domain.application.file.FileVariable.ROOT_DIRECTORY
import com.music_service.global.exception.MusicServiceException.MusicFileUploadFailException
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

@Component
class FileHandlerImpl(
    private val musicHandler: MusicHandlerImpl,
    private val imageHandler: ImageHandlerImpl
): FileHandler {

    override fun upload(file: MultipartFile, filename: String) {
        try {
            val path = Paths.get("$FILE_DIRECTORY/$filename")
            val dirPath = path.parent
            Files.notExists(dirPath).run { Files.createDirectories(dirPath) }
            Files.copy(file.inputStream, path)
        } catch (e: IOException) {
            throw MusicFileUploadFailException("File upload failed: ${file.originalFilename}")
        }
    }

    override fun delete(fileUrl: String) =
        File("$ABSOLUTE_PATH$ROOT_DIRECTORY$fileUrl").let {
            if (it.exists()) {
                if (!it.delete())
                    throw IOException("Failed to delete file: $fileUrl")
            }
            else
                throw IOException("File does not exist: $fileUrl")
        }

    override fun downloadMusic(fileUrl: String): FileDownloadResponseDTO =
        musicHandler.downloadMusic(fileUrl)

    override fun displayImage(fileUrl: String): Resource =
        imageHandler.displayImage(fileUrl)
}