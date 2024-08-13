package com.music_service.domain.application.file

import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

abstract class AbstractFileProcessor: FileProcessor {

    companion object {
        internal val ABSOLUTE_PATH: String = File("").absolutePath
        internal const val ROOT_DIRECTORY = "/music-service/src/main/resources/file/"
        private val CURRENT_DATE = SimpleDateFormat("yy-MM-dd").format(Date())
        private val FILE_DIRECTORY = ABSOLUTE_PATH + ROOT_DIRECTORY + CURRENT_DATE
    }

    override fun upload(file: MultipartFile?): Triple<String, String, String> {
        file?.let { f ->
            val originalFilename = f.originalFilename ?: throw IOException("Original filename is null")
            val extension = originalFilename.substring(originalFilename.lastIndexOf("."))
            val uuid = UUID.randomUUID().toString()
            val savedName = "$uuid$extension"

            val path = Paths.get("$FILE_DIRECTORY/$savedName")
            val dirPath = path.parent
            Files.notExists(dirPath).run {
                Files.createDirectories(dirPath)
            }

            Files.copy(f.inputStream, path)
            return Triple(savedName, uuid, "$CURRENT_DATE/$savedName")

        } ?: throw IOException("Unable to upload music")
    }

    override fun delete(fileUrl: String) = File("$ABSOLUTE_PATH$ROOT_DIRECTORY$fileUrl").let {
        if (it.exists()) {
            if (!it.delete())
                throw IOException("Failed to delete file: $fileUrl")
        }
        else
            throw IOException("File does not exist: $fileUrl")
    }
}