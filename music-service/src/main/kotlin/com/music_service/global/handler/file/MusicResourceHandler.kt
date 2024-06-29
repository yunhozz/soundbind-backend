package com.music_service.global.handler.file

import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

@Component
abstract class MusicResourceHandler: ResourceHandler {
    override fun download(fileUrl: String): Resource {
        TODO("Not yet implemented")
    }
}