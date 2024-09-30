package com.music_service.domain.interfaces

import com.music_service.domain.application.MusicSearchService
import com.music_service.domain.interfaces.dto.APIResponse
import com.music_service.domain.persistence.repository.MusicSort
import com.music_service.domain.persistence.repository.dto.MusicCursorDTO
import com.sound_bind.music_service.global.annotation.HeaderSubject
import io.swagger.v3.oas.annotations.Operation
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
class MusicSearchController(private val musicSearchService: MusicSearchService) {

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "음원 상세 조회")
    fun lookUpMusicDetails(@HeaderSubject sub: String, @PathVariable id: String): APIResponse {
        val musicDetails = musicSearchService.findMusicDetailsByElasticsearch(id.toLong(), sub.toLong())
        return APIResponse.of("Music Details Found", musicDetails)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "음원 키워드 커서 페이징 조회 (default)")
    fun findMusicSimpleListByKeywordDefault(
        @HeaderSubject sub: String,
        @RequestParam(required = true) keyword: String
    ): APIResponse {
        val musics = musicSearchService.findMusicSimpleListByKeywordAndCondition(
            keyword,
            MusicSort.ACCURACY,
            cursor = null,
            sub.toLong()
        )
        return APIResponse.of("Music List Found", musics)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "음원 키워드 커서 페이징 조회")
    fun findMusicSimpleListByKeywordAndCondition(
        @HeaderSubject sub: String,
        @RequestParam(required = true) keyword: String,
        @RequestParam(required = false, defaultValue = "accuracy") sort: String,
        @RequestBody(required = false) cursor: MusicCursorDTO?
    ): APIResponse {
        val musics = musicSearchService.findMusicSimpleListByKeywordAndCondition(
            keyword,
            MusicSort.of(sort),
            cursor,
            sub.toLong()
        )
        return APIResponse.of("Music List Found", musics)
    }
}