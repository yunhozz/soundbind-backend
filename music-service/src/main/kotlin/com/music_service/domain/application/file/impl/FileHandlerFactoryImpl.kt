package com.music_service.domain.application.file.impl

import com.music_service.domain.application.file.FileHandlerFactory
import com.music_service.domain.application.file.ImageHandler
import com.music_service.domain.application.file.MusicHandler
import org.springframework.stereotype.Component

@Component
class FileHandlerFactoryImpl: FileHandlerFactory {
    override fun createFileHandler() = FileHandlerImpl()
    override fun createMusicHandler(): MusicHandler = MusicHandlerImpl()
    override fun createImageHandler(): ImageHandler = ImageHandlerImpl()
}