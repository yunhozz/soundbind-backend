package com.music_service.domain.application.file

import com.music_service.domain.application.dto.response.FileDownloadResponseDTO
import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import com.music_service.global.exception.MusicServiceException.MusicFileUploadFailException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

@Component
class FileHandler: FileHandlerFactory {
    private val musicHandler = createMusicHandler()
    private val imageHandler = createImageHandler()
    private val log: Logger = LoggerFactory.getLogger(FileHandlerFactory::class.java)

    final override fun createMusicHandler(): MusicHandler = MusicHandlerImpl()

    final override fun createImageHandler(): ImageHandler = ImageHandlerImpl()

    fun generateFileInfo(file: MultipartFile): FileUploadResponseDTO {
        val originalFilename = file.originalFilename
            ?: throw IOException("Original filename is null")
        val extension = originalFilename.substring(originalFilename.lastIndexOf("."))
        val uuid = UUID.randomUUID().toString()
        val savedName = "$uuid$extension"

        log.info("File Upload Success : $savedName")

        return FileUploadResponseDTO(
            file,
            originalFilename,
            savedName,
            fileUrl = "$CURRENT_DATE/$savedName"
        )
    }

    fun upload(file: MultipartFile, filename: String) {
        try {
            val path = Paths.get("$FILE_DIRECTORY/$filename")
            val dirPath = path.parent
            Files.notExists(dirPath).run { Files.createDirectories(dirPath) }
            Files.copy(file.inputStream, path)
        } catch (e: IOException) {
            log.error(e.localizedMessage)
            throw MusicFileUploadFailException("File upload failed: ${file.originalFilename}")
        }
    }

    fun delete(fileUrl: String) =
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

    companion object {
        internal val ABSOLUTE_PATH: String = File("").absolutePath
        internal const val ROOT_DIRECTORY = "/music-service/src/main/resources/file/"
        internal val CURRENT_DATE: String = SimpleDateFormat("yy-MM-dd").format(Date())
        internal val FILE_DIRECTORY = ABSOLUTE_PATH + ROOT_DIRECTORY + CURRENT_DATE
    }
}