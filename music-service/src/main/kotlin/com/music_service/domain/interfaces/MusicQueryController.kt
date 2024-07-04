package com.music_service.domain.interfaces

import com.music_service.domain.application.MusicService
import com.music_service.domain.interfaces.dto.APIResponse
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/music/q")
class MusicQueryController(
    private val musicService: MusicService
) {
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun searchMusicsByKeyword(
        @RequestParam(required = true) keyword: String,
        pageable: Pageable
    ): APIResponse {
        val result = musicService.findMusicsByKeyword(keyword, pageable)
        return APIResponse.of("Music Keyword Search List Found", result)
    }
}