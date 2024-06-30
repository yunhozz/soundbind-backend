package com.music_service.global.handler.file

import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

@Component
class MusicResourceHandlerImpl: MusicResourceHandler {
    override fun downloadMusic(fileUrl: String): Resource {
        TODO("Not yet implemented")
    }

    override lateinit var originalFileName: String

    override lateinit var savedName: String

    override lateinit var fileUrl: String
}