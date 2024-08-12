package com.music_service.domain.application.file

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class ImageFileProcessorImpl: ImageFileProcessor {

    private val log = LoggerFactory.getLogger(ImageFileProcessorImpl::class.java)
    private val fileProcessor: FileProcessor = object : AbstractFileProcessor() {}

    override fun upload(file: MultipartFile?): Triple<String, String, String> {
        log.debug("Uploading image")
        return fileProcessor.upload(file)
    }

    override fun update(fileUrl: String, file: MultipartFile?) {
        log.debug("Updating image : $fileUrl")
        fileProcessor.delete(fileUrl)
        fileProcessor.upload(file)
    }

    override fun delete(fileUrl: String) {
        log.debug("Deleting image : $fileUrl")
        fileProcessor.delete(fileUrl)
    }
}