package com.music_service.domain.application.file

import org.springframework.core.io.InputStreamResource
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

sealed interface ResourceHandler {
    companion object {
        private val ABSOLUTE_PATH: String = File("").absolutePath
        private const val ROOT_DIRECTORY = "/music-service/src/main/resources/file/"
        private val CURRENT_DATE = SimpleDateFormat("yy-MM-dd").format(Date())
        private val FILE_DIRECTORY = ABSOLUTE_PATH + ROOT_DIRECTORY + CURRENT_DATE
    }

    fun upload(file: MultipartFile?): Triple<String, String, String> {
        file?.let {
            val originalFileName = file.originalFilename!!
            val extension = originalFileName.substring(originalFileName.lastIndexOf("."))
            val uuid = UUID.randomUUID().toString()
            val savedName = uuid + extension
            val fileUrl = "$CURRENT_DATE/$savedName"

            File("$FILE_DIRECTORY/$savedName").also {
                if (!it.exists()) it.mkdirs()
                file.transferTo(it)
            }
            return Triple(savedName, uuid, fileUrl)

        } ?: throw IOException("File does not exist.")
    }

    fun download(fileUrl: String): Pair<Resource, String> {
        val path = Paths.get(ABSOLUTE_PATH + ROOT_DIRECTORY + fileUrl)
        val inputStream = Files.newInputStream(path)
        val contentType = Files.probeContentType(path)
        return Pair(InputStreamResource(inputStream), contentType)
    }

    fun delete(fileUrl: String) = File("$ABSOLUTE_PATH$ROOT_DIRECTORY$fileUrl").let {
        if (it.exists()) {
            if (!it.delete())
                throw IOException("Failed to delete file: $fileUrl")
        }
        else
            throw IOException("File does not exist: $fileUrl")
    }
}

@Component
class MusicResourceHandler: ResourceHandler {
    fun uploadMusic(file: MultipartFile): Triple<String, String, String> = upload(file)
    fun downloadMusic(fileUrl: String): Pair<Resource, String> = download(fileUrl)
    fun deleteMusic(fileUrl: String) = delete(fileUrl)
}

@Component
class ImageResourceHandler: ResourceHandler {
    fun uploadImage(file: MultipartFile): Triple<String, String, String> = upload(file)
    fun deleteImage(fileUrl: String) = delete(fileUrl)
}