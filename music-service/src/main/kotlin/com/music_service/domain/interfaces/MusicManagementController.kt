package com.music_service.domain.interfaces

import com.music_service.domain.application.MusicService
import com.music_service.domain.application.dto.request.MusicCreateDTO
import com.music_service.domain.application.dto.request.MusicUpdateDTO
import com.music_service.domain.interfaces.dto.APIResponse
import com.sound_bind.music_service.global.annotation.HeaderSubject
import io.swagger.v3.oas.annotations.Operation
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
@RequestMapping("/api/musics")
class MusicManagementController(private val musicService: MusicService) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "음원 업로드")
    fun uploadMusic(
        @HeaderSubject sub: String,
        @Valid @ModelAttribute dto: MusicCreateDTO
    ): APIResponse {
        val musicId = musicService.uploadMusic(sub.toLong(), dto)
        return APIResponse.of("Music Created", musicId)
    }

    @GetMapping("/{id}/download")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "음원 다운로드")
    fun downloadMusic(@PathVariable("id") id: String): ResponseEntity<Resource> {
        val music = musicService.downloadMusic(id.toLong())
        val disposition = ContentDisposition.builder("attachment")
            .filename(UriUtils.encode(music.fileName, StandardCharsets.UTF_8))
            .build()
        val headers = HttpHeaders().apply {
            contentDisposition = disposition
            contentType = MediaType.valueOf(music.contentType)
        }
        return ResponseEntity(music.musicFile, headers, HttpStatus.OK)
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "음원 정보 업데이트")
    fun updateMusicInformation(
        @HeaderSubject sub: String,
        @PathVariable("id") id: String,
        @Valid @ModelAttribute dto: MusicUpdateDTO
    ): APIResponse {
        val musicId = musicService.updateMusicInformation(id.toLong(), dto)
        return APIResponse.of("Music Updated", musicId)
    }

    @PostMapping("/{id}/likes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "음원 좋아요")
    fun updateLikesOnMusic(@HeaderSubject sub: String, @PathVariable("id") id: String): APIResponse {
        musicService.changeLikesFlag(id.toLong(), sub.toLong())
        return APIResponse.of("Likes of Music Changed")
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "음원 삭제")
    fun deleteMusic(@HeaderSubject sub: String, @PathVariable("id") id: String): APIResponse {
        musicService.deleteMusic(id.toLong())
        return APIResponse.of("Music Deleted")
    }
}