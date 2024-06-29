package com.music_service.global.handler.file

import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

interface ResourceHandler {
    companion object {
        private val ABSOLUTE_PATH = File("").absolutePath
        private val CURRENT_DATE = SimpleDateFormat("yy-MM-dd").format(Date())
        private const val ROOT_DIRECTORY = "/music_service/src/main/resources/file"
        val FILE_DIRECTORY = ABSOLUTE_PATH + ROOT_DIRECTORY
    }

    @Throws(IOException::class)
    fun upload(file: MultipartFile?) {
        file?.let {
            val originalFilename = file.originalFilename!!
            val extension = originalFilename.substring(originalFilename.lastIndexOf("."))
            val uuid = UUID.randomUUID().toString()
            val savedName = uuid + extension

            File("$FILE_DIRECTORY/$CURRENT_DATE/$savedName").also {
                if (!it.exists()) it.mkdirs()
                file.transferTo(it)
            }
        } ?: throw IllegalArgumentException("File does not exist.")
    }
}

interface MusicResourceHandler: ResourceHandler {
    @Throws(IOException::class)
    fun uploadMusic(file: MultipartFile) = upload(file)

    @Throws(IOException::class)
    fun downloadMusic(fileUrl: String): Resource
}

interface ImageResourceHandler: ResourceHandler {
    @Throws(IOException::class)
    fun uploadImage(file: MultipartFile) = upload(file)

    @Throws(IOException::class)
    fun displayImage(fileUrl: String): Resource
}