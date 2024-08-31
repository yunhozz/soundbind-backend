package com.music_service.domain.application.file.impl

import com.music_service.domain.application.dto.response.FileDownloadResponseDTO
import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import com.music_service.domain.application.file.FileHandler
import com.music_service.domain.application.file.FileVariable.ABSOLUTE_PATH
import com.music_service.domain.application.file.FileVariable.CURRENT_DATE
import com.music_service.domain.application.file.FileVariable.FILE_DIRECTORY
import com.music_service.domain.application.file.FileVariable.ROOT_DIRECTORY
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

@Component
class FileHandlerImpl(
    private val musicHandler: MusicHandlerImpl,
    private val imageHandler: ImageHandlerImpl
): FileHandler {

    override fun upload(file: MultipartFile?): FileUploadResponseDTO {
        file?.let { f ->
            val originalFilename = f.originalFilename
                ?: throw IOException("Original filename is null")
            val extension = originalFilename.substring(originalFilename.lastIndexOf("."))
            val uuid = UUID.randomUUID().toString()
            val savedName = "$uuid$extension"

            val path = Paths.get("$FILE_DIRECTORY/$savedName")
            val dirPath = path.parent
            Files.notExists(dirPath).run { Files.createDirectories(dirPath) }
            Files.copy(f.inputStream, path)

            return FileUploadResponseDTO(
                originalFilename,
                savedName,
                "$CURRENT_DATE/$savedName"
            )
        } ?: throw IOException("Unable to upload music")
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