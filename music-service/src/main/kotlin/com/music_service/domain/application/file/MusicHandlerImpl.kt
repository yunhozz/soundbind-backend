package com.music_service.domain.application.file

import com.music_service.domain.application.dto.response.FileDownloadResponseDTO
import com.music_service.domain.application.file.FileHandler.Companion.ABSOLUTE_PATH
import com.music_service.domain.application.file.FileHandler.Companion.ROOT_DIRECTORY
import org.springframework.core.io.InputStreamResource
import java.nio.file.Files
import java.nio.file.Paths

class MusicHandlerImpl: MusicHandler {

    override fun downloadMusic(fileUrl: String): FileDownloadResponseDTO {
        val path = Paths.get(ABSOLUTE_PATH + ROOT_DIRECTORY + fileUrl)
        val inputStream = Files.newInputStream(path)
        val contentType = Files.probeContentType(path)
        return FileDownloadResponseDTO(InputStreamResource(inputStream), contentType)
    }
}