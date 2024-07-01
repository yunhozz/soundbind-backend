package com.music_service.domain.interfaces

import com.music_service.domain.application.MusicService
import com.music_service.global.dto.request.MusicCreateDTO
import com.music_service.global.dto.response.APIResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/music")
class MusicController(
    private val musicService: MusicService
) {
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun uploadMusic(@Valid @ModelAttribute dto: MusicCreateDTO): APIResponse {
        val musicId = musicService.uploadMusic(dto)
        return APIResponse.of("Music Uploaded", musicId)
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun getMusic(@PathVariable("id") id: String): APIResponse {
        val musicDetails = musicService.findMusicDetails(id.toLong())
        return APIResponse.of("Music Found", musicDetails)
    }
}