package com.music_service.global.handler.file

import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

@Component
abstract class ImageResourceHandler: ResourceHandler {
    override fun display(fileUrl: String): Resource {
        TODO("Not yet implemented")
    }
}