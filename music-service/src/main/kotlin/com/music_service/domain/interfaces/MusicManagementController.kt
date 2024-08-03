package com.music_service.domain.interfaces

import com.music_service.domain.application.MusicService
import com.music_service.domain.application.dto.request.MusicCreateDTO
import com.music_service.domain.application.dto.request.MusicUpdateDTO
import com.music_service.domain.interfaces.dto.APIResponse
import jakarta.validation.Valid
import org.springframework.core.io.Resource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriUtils
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/api/music")
class MusicManagementController(
    private val musicService: MusicService
) {
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun uploadMusic(@Valid @ModelAttribute dto: MusicCreateDTO): APIResponse {
        val musicId = musicService.uploadMusic(dto)
        return APIResponse.of("Music Created", musicId)
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun getMusic(@PathVariable("id") id: String): APIResponse {
        val musicDetails = musicService.findMusicDetails(id.toLong())
        return APIResponse.of("Music Found", musicDetails)
    }

    @GetMapping("/{id}/download")
    @ResponseStatus(HttpStatus.OK)
    fun downloadMusic(@PathVariable("id") id: String): ResponseEntity<Resource> {
        val music = musicService.downloadMusic(id.toLong())
        val disposition = ContentDisposition.builder("attachment")
            .filename(UriUtils.encode(music.fileName, StandardCharsets.UTF_8))
            .build()

        val headers = HttpHeaders()
        headers.contentDisposition = disposition
        headers.contentType = MediaType.valueOf(music.contentType)

        return ResponseEntity(music.musicFile, headers, HttpStatus.OK)
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    fun updateMusicInformation(
        @PathVariable("id") id: String,
        @Valid @ModelAttribute dto: MusicUpdateDTO
    ): APIResponse {
        val musicId = musicService.updateMusic(id.toLong(), dto)
        return APIResponse.of("Music Updated", musicId)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteMusic(@PathVariable("id") id: String): APIResponse {
        musicService.deleteMusic(id.toLong())
        return APIResponse.of("Music Deleted")
    }
}