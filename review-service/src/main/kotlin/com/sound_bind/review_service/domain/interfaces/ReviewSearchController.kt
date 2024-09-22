package com.sound_bind.review_service.domain.interfaces

import com.review_service.domain.interfaces.dto.APIResponse
import com.sound_bind.review_service.domain.application.ElasticsearchService
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewCursorDTO
import com.sound_bind.review_service.global.annotation.HeaderSubject
import com.sound_bind.review_service.global.enums.ReviewSort
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
    fun lookUpReviewsInMusicByDefault(@HeaderSubject sub: String, @RequestParam musicId: String): APIResponse {
        val reviews = elasticsearchService.findReviewListByMusicIdV2(
            musicId.toLong(),
            userId = sub.toLong(),
            reviewSort = ReviewSort.LIKES,
            dto = null
        )
        return APIResponse.of("Reviews found", reviews)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun lookupReviewsInMusicByConditionsV2(
        @HeaderSubject sub: String,
        @RequestParam musicId: String,
        @RequestParam(required = false, defaultValue = "likes") sort: String,
        @RequestBody(required = false) dto: ReviewCursorDTO,
    ): APIResponse {
        val reviews = elasticsearchService.findReviewListByMusicIdV2(
            musicId.toLong(),
            userId = sub.toLong(),
            reviewSort = ReviewSort.of(sort),
            dto
        )
        return APIResponse.of("Reviews found", reviews)
    }
}