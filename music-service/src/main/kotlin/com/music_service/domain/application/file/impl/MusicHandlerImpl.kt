package com.music_service.domain.application.file.impl

import com.music_service.domain.application.dto.response.FileDownloadResponseDTO
import com.music_service.domain.application.file.FileVariable.ABSOLUTE_PATH
import com.music_service.domain.application.file.FileVariable.ROOT_DIRECTORY
import com.music_service.domain.application.file.MusicHandler
import org.springframework.core.io.InputStreamResource
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths

@Component
class MusicHandlerImpl: MusicHandler {

    override fun downloadMusic(fileUrl: String): FileDownloadResponseDTO {
        val path = Paths.get(ABSOLUTE_PATH + ROOT_DIRECTORY + fileUrl)
        val inputStream = Files.newInputStream(path)
        val contentType = Files.probeContentType(path)
        return FileDownloadResponseDTO(InputStreamResource(inputStream), contentType)
    }
}