package com.sound_bind.review_service.domain.interfaces

import com.review_service.domain.interfaces.dto.APIResponse
import com.sound_bind.review_service.domain.application.ReviewService
import com.sound_bind.review_service.global.dto.request.ReviewCreateDTO
import jakarta.validation.Valid
import khttp.get
import org.springframework.http.HttpStatus
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
}