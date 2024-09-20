package com.music_service.domain.interfaces

import com.music_service.domain.application.ElasticsearchService
import com.music_service.domain.interfaces.dto.APIResponse
import com.music_service.domain.persistence.repository.MusicSort
import com.music_service.domain.persistence.repository.dto.MusicCursorDTO
import com.sound_bind.music_service.global.annotation.HeaderSubject
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/musics/search")
class MusicSearchController(private val elasticsearchService: ElasticsearchService) {

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun lookUpMusicDetails(@HeaderSubject sub: String, @PathVariable id: String): APIResponse {
        val result = elasticsearchService.findMusicDetailsByElasticsearch(id.toLong(), sub.toLong())
        return APIResponse.of("Music Details Found", result)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun findMusicSimpleListByKeywordDefault(
        @HeaderSubject sub: String,
        @RequestParam(required = true) keyword: String
    ): APIResponse {
        val result = elasticsearchService.findMusicSimpleListByKeywordAndCondition(
            keyword,
            MusicSort.ACCURACY,
            cursor = null,
            sub.toLong()
        )
        return APIResponse.of("Music List Found", result)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    fun findMusicSimpleListByKeywordAndCondition(
        @HeaderSubject sub: String,
        @RequestParam(required = true) keyword: String,
        @RequestParam(required = false, defaultValue = "accuracy") sort: String,
        @RequestBody(required = false) cursor: MusicCursorDTO?
    ): APIResponse {
        val result = elasticsearchService.findMusicSimpleListByKeywordAndCondition(
            keyword,
            MusicSort.of(sort),
            cursor,
            sub.toLong()
        )
        return APIResponse.of("Music List Found", result)
    }
}