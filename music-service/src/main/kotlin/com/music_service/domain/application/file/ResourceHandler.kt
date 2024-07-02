package com.music_service.domain.application.file

import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

sealed interface ResourceHandler {
    companion object {
        private const val ROOT_DIRECTORY = "/music-service/src/main/resources/file/"
        private val ABSOLUTE_PATH = File("").absolutePath
        private val CURRENT_DATE = SimpleDateFormat("yy-MM-dd").format(Date())
        private val FILE_DIRECTORY = ABSOLUTE_PATH + ROOT_DIRECTORY + CURRENT_DATE
    }

    var originalFileName: String
    var savedName: String
    var fileUrl: String

    @Throws(IOException::class)
    fun upload(file: MultipartFile?) {
        file?.let {
            originalFileName = file.originalFilename!!
            val extension = originalFileName.substring(originalFileName.lastIndexOf("."))
            val uuid = UUID.randomUUID().toString()
            savedName = uuid + extension
            fileUrl = "$CURRENT_DATE/$savedName"

            File("$FILE_DIRECTORY/$savedName").also {
                if (!it.exists()) it.mkdirs()
                file.transferTo(it)
            }
        } ?: throw IllegalArgumentException("File does not exist.")
    }
}

interface MusicResourceHandler: ResourceHandler {
    @Throws(IOException::class)
    fun uploadMusic(file: MultipartFile): Triple<String, String, String> {
        upload(file)
        return Triple(originalFileName, savedName, fileUrl)
    }

    @Throws(IOException::class)
    fun downloadMusic(fileUrl: String): Resource
}

interface ImageResourceHandler: ResourceHandler {
    @Throws(IOException::class)
    fun uploadImage(file: MultipartFile): Triple<String, String, String> {
        upload(file)
        return Triple(originalFileName, savedName, fileUrl)
    }

    @Throws(IOException::class)
    fun displayImage(fileUrl: String): Resource
}