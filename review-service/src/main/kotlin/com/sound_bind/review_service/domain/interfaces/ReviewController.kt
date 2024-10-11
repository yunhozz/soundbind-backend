package com.sound_bind.review_service.domain.interfaces

import com.sound_bind.global.annotation.HeaderSubject
import com.sound_bind.global.dto.ApiResponse
import com.sound_bind.review_service.domain.application.ReviewService
import com.sound_bind.review_service.domain.application.dto.request.ReviewCreateDTO
import com.sound_bind.review_service.domain.application.dto.request.ReviewUpdateDTO
import com.sound_bind.review_service.domain.application.dto.response.ReviewDetailsDTO
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewCursorDTO
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewQueryDTO
import com.sound_bind.review_service.global.enums.ReviewSort
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
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
class ReviewController(private val reviewService: ReviewService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "음원에 대한 리뷰 작성")
    fun createReviewOnMusic(
        @HeaderSubject sub: String,
        @RequestParam musicId: String,
        @Valid @RequestBody dto: ReviewCreateDTO
    ): ApiResponse<Long> {
        val reviewId = reviewService.createReview(musicId.toLong(), sub.toLong(), dto)
        return ApiResponse.of("Review created", reviewId)
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "리뷰 상세 조회")
    fun lookupDetailsOfReview(@PathVariable id: String): ApiResponse<ReviewDetailsDTO> {
        val reviewDetails = reviewService.lookupDetailsOfReviewById(id.toLong())
        return ApiResponse.of("Review found", reviewDetails)
    }

    @PostMapping("/found")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "음원에 대한 리뷰 커서 페이징 조회 Ver.1")
    fun lookupReviewsInMusicByConditionsV1(
        @HeaderSubject sub: String,
        @RequestParam musicId: String,
        @RequestParam(required = false, defaultValue = "likes") sort: String,
        @RequestParam(required = false, defaultValue = "0") page: String,
        @RequestBody(required = false) dto: ReviewCursorDTO,
    ): ApiResponse<Slice<ReviewQueryDTO>> {
        val reviews = reviewService.findReviewListByMusicIdV1(
            musicId.toLong(),
            userId = sub.toLong(),
            reviewSort = ReviewSort.of(sort),
            dto,
            pageable = PageRequest.of(page.toInt(), 20)
        )
        return ApiResponse.of("Reviews found", reviews)
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "리뷰 업데이트")
    fun updateReview(
        @HeaderSubject sub: String,
        @PathVariable("id") id: String,
        @Valid @RequestBody dto: ReviewUpdateDTO
    ): ApiResponse<Long> {
        val reviewId = reviewService.updateReviewMessageAndScore(id.toLong(), sub.toLong(), dto)
        return ApiResponse.of("Review updated", reviewId)
    }

    @PostMapping("/{id}/likes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "리뷰 좋아요")
    fun updateLikesOnReview(
        @HeaderSubject sub: String,
        @PathVariable("id") id: String
    ): ApiResponse<Unit> {
        reviewService.changeLikesFlag(id.toLong(), sub.toLong())
        return ApiResponse.of("Likes of Review Changed")
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "리뷰 삭제")
    fun deleteReview(
        @HeaderSubject sub: String,
        @PathVariable("id") id: String
    ): ApiResponse<Unit> {
        reviewService.deleteReview(id.toLong(), sub.toLong())
        return ApiResponse.of("Review deleted")
    }
}