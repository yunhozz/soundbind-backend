package com.music_service.domain.application.file

import com.music_service.domain.application.file.AbstractFileProcessor.Companion.ABSOLUTE_PATH
import com.music_service.domain.application.file.AbstractFileProcessor.Companion.ROOT_DIRECTORY
import org.slf4j.LoggerFactory
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths

@Component
class MusicFileProcessorImpl: MusicFileProcessor {

    private val log = LoggerFactory.getLogger(MusicFileProcessorImpl::class.java)
    private val fileProcessor = object : AbstractFileProcessor() {}

    override fun upload(file: MultipartFile?): Triple<String, String, String> {
        log.debug("Uploading music")
        return fileProcessor.upload(file)
    }

    override fun download(fileUrl: String): Pair<Resource, String> {
        log.debug("Downloading music : $fileUrl")
        val path = Paths.get(ABSOLUTE_PATH + ROOT_DIRECTORY + fileUrl)
        val inputStream = Files.newInputStream(path)
        val contentType = Files.probeContentType(path)
        return Pair(InputStreamResource(inputStream), contentType)
    }

    override fun delete(fileUrl: String) {
        log.debug("Deleting music : $fileUrl")
        fileProcessor.delete(fileUrl)
    }
}