package com.sound_bind.review_service.domain.interfaces

import com.review_service.domain.interfaces.dto.APIResponse
import com.sound_bind.review_service.domain.application.ReviewService
import com.sound_bind.review_service.global.dto.request.ReviewCreateDTO
import com.sound_bind.review_service.global.dto.request.ReviewCursorRequestDTO
import com.sound_bind.review_service.global.dto.request.ReviewUpdateDTO
import jakarta.validation.Valid
import khttp.get
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/reviews")
class ReviewController(
    private val reviewService: ReviewService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createReviewOnMusic(
        @RequestParam musicId: String,
        @Valid @RequestBody dto: ReviewCreateDTO
    ): APIResponse {
        val reviewId = reviewService.createReview(musicId.toLong(), 123L, dto)
        val response = get("http://localhost:8000/api/music/$musicId") // Check music exist
        if (response.statusCode != HttpStatus.OK.value()) {
            val message = response.jsonObject.getString("message")
            return APIResponse.of(message)
        }
        return APIResponse.of("Review created", reviewId)
    }

    @PostMapping("/found")
    @ResponseStatus(HttpStatus.CREATED)
    fun findReviewsOnMusic(
        @RequestParam musicId: String,
        @RequestParam(required = false, defaultValue = "likes") sort: String,
        @RequestBody dto: ReviewCursorRequestDTO,
        @PageableDefault(size = 20) pageable: Pageable
    ): APIResponse {
        val result = reviewService.findReviewListByMusicId(musicId.toLong(), 123L, sort, dto, pageable)
        return APIResponse.of("Reviews found", result)
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    fun updateReview(
        @PathVariable("id") id: String,
        @Valid @RequestBody dto: ReviewUpdateDTO
    ): APIResponse {
        val reviewId = reviewService.updateReviewMessageAndScore(id.toLong(), 123L, dto)
        return APIResponse.of("Review updated", reviewId)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteReview(@PathVariable("id") id: String): APIResponse {
        reviewService.deleteReview(id.toLong(), 123L)
        return APIResponse.of("Review deleted")
    }
}