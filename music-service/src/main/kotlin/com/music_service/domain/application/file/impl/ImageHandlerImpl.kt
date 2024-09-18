package com.music_service.domain.application.file.impl

import com.music_service.domain.application.file.ImageHandler
import com.music_service.domain.application.file.impl.FileHandlerImpl.Companion.ABSOLUTE_PATH
import com.music_service.domain.application.file.impl.FileHandlerImpl.Companion.ROOT_DIRECTORY
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import java.nio.file.Files
import java.nio.file.Paths

class ImageHandlerImpl: ImageHandler {

    override fun displayImage(fileUrl: String): Resource {
        val path = Paths.get(ABSOLUTE_PATH + ROOT_DIRECTORY + fileUrl)
        val inputStream = Files.newInputStream(path)
        val contentType = Files.probeContentType(path)
        return InputStreamResource(inputStream)
    }
}