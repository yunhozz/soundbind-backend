package com.music_service.domain.application.file

interface FileHandlerFactory {
    fun createFileHandler(): FileHandler
    fun createMusicHandler(): MusicHandler
    fun createImageHandler(): ImageHandler
}