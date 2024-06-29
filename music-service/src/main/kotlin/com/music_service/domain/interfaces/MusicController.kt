package com.music_service.domain.interfaces

import com.music_service.domain.application.MusicService
import com.music_service.global.dto.request.MusicCreateDTO
import com.music_service.global.dto.response.APIResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/music")
class MusicController(
    private val musicService: MusicService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun uploadMusic(dto: MusicCreateDTO): APIResponse {
        val musicId = musicService.uploadMusic(dto)
        return APIResponse.of("Music Uploaded", musicId)
    }
}