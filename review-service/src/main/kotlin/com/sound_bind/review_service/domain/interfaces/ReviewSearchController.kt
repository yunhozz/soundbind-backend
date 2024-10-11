package com.sound_bind.review_service.domain.interfaces

import com.sound_bind.global.annotation.HeaderSubject
import com.sound_bind.global.dto.ApiResponse
import com.sound_bind.review_service.domain.application.ElasticsearchService
import com.sound_bind.review_service.domain.persistence.es.ReviewDocument
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewCursorDTO
import com.sound_bind.review_service.global.enums.ReviewSort
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/reviews/search")
class ReviewSearchController(private val elasticsearchService: ElasticsearchService) {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "음원에 대한 리뷰 커서 페이징 조건 조회 (default)")
    fun lookUpReviewsInMusicByDefault(
        @HeaderSubject sub: String,
        @RequestParam musicId: String
    ): ApiResponse<List<ReviewDocument?>> {
        val reviews = elasticsearchService.findReviewListByMusicIdV2(
            musicId.toLong(),
            userId = sub.toLong(),
            reviewSort = ReviewSort.LIKES,
            dto = null
        )
        return ApiResponse.of("Reviews found", reviews)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "음원에 대한 리뷰 커서 페이징 조건 조회 Ver.2")
    fun lookupReviewsInMusicByConditionsV2(
        @HeaderSubject sub: String,
        @RequestParam musicId: String,
        @RequestParam(required = false, defaultValue = "likes") sort: String,
        @RequestBody(required = false) dto: ReviewCursorDTO,
    ): ApiResponse<List<ReviewDocument?>> {
        val reviews = elasticsearchService.findReviewListByMusicIdV2(
            musicId.toLong(),
            userId = sub.toLong(),
            reviewSort = ReviewSort.of(sort),
            dto
        )
        return ApiResponse.of("Reviews found", reviews)
    }
}