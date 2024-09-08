package com.music_service.domain.interfaces

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.music_service.domain.application.MusicService
import com.music_service.domain.application.dto.request.MusicCreateDTO
import com.music_service.domain.application.dto.request.MusicUpdateDTO
import com.music_service.domain.interfaces.dto.APIResponse
import com.music_service.domain.interfaces.dto.KafkaRequestDTO
import com.music_service.global.util.RedisUtils
import com.sound_bind.music_service.global.annotation.HeaderSubject
import jakarta.validation.Valid
import khttp.post
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
    fun uploadMusic(
        @HeaderSubject sub: String,
        @Valid @ModelAttribute dto: MusicCreateDTO
    ): APIResponse {
        val musicId = musicService.uploadMusic(sub.toLong(), dto)
        return APIResponse.of("Music Created", musicId)
    }

    @GetMapping("/{id}/musician")
    @ResponseStatus(HttpStatus.OK)
    fun lookUpMusicianId(@PathVariable id: String): APIResponse {
        val musicianId = musicService.findMusicianIdByMusicId(id.toLong())
        return APIResponse.of("Musician ID Found", musicianId)
    }

    @GetMapping("/{id}/download")
    @ResponseStatus(HttpStatus.OK)
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
    fun updateLikesOnMusic(@HeaderSubject sub: String, @PathVariable("id") id: String): APIResponse {
        musicService.changeLikesFlag(id.toLong(), sub.toLong())?.let {
            val myInfo = RedisUtils.getJson("user:$sub", Map::class.java)
                ?: throw IllegalArgumentException("Value is not Present by Key : user:$sub")
            val record = KafkaRequestDTO(
                topic = "music-like-topic",
                message = KafkaRequestDTO.KafkaNotificationDTO(
                    userId = it,
                    content = "${myInfo["nickname"] as String} 님이 당신의 음원에 좋아요를 눌렀습니다.",
                    link = null
                )
            )
            sendMessageToKafkaProducer(record)
        }
        return APIResponse.of("Likes of Music Changed")
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteMusic(@HeaderSubject sub: String, @PathVariable("id") id: String): APIResponse {
        musicService.deleteMusic(id.toLong())
        return APIResponse.of("Music Deleted")
    }

    private fun sendMessageToKafkaProducer(record: KafkaRequestDTO) =
        post(
            url = "http://localhost:9000/api/kafka",
            headers = mapOf("Content-Type" to "application/json"),
            data = mapper.writeValueAsString(record)
        )

    companion object {
        private val mapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
}