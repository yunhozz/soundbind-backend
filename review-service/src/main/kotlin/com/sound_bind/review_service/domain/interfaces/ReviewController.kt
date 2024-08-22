package com.sound_bind.review_service.domain.interfaces

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.review_service.domain.interfaces.dto.APIResponse
import com.sound_bind.review_service.domain.application.ReviewAsyncService
import com.sound_bind.review_service.domain.application.ReviewService
import com.sound_bind.review_service.domain.application.dto.request.ReviewCreateDTO
import com.sound_bind.review_service.domain.application.dto.request.ReviewUpdateDTO
import com.sound_bind.review_service.domain.interfaces.dto.KafkaRecordDTO
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewCursorRequestDTO
import com.sound_bind.review_service.global.annotation.HeaderSubject
import jakarta.validation.Valid
import khttp.get
import khttp.post
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
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
class ReviewController(
    private val reviewService: ReviewService,
    private val reviewAsyncService: ReviewAsyncService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createReviewOnMusic(
        @HeaderSubject sub: String,
        @RequestParam musicId: String,
        @Valid @RequestBody dto: ReviewCreateDTO
    ): APIResponse {
        val response = get("http://localhost:8070/api/music/$musicId") // Check music exist
        if (response.statusCode != HttpStatus.OK.value()) {
            val message = response.jsonObject.getString("message")
            return APIResponse.of(message)
        }
        val reviewDetailsDTO = reviewService.createReview(musicId.toLong(), sub.toLong(), dto)
        reviewAsyncService.indexReviewInElasticSearch(reviewDetailsDTO)

        val obj = mapper.readValue(response.text, Map::class.java)
        val data = mapper.readValue(mapper.writeValueAsString(obj["data"]), Map::class.java)
        val myInfo = reviewService.getUserInformationOnRedis(sub.toLong())

        val record = KafkaRecordDTO(
            "review-added-topic",
            data["userId"].toString(),
            "${myInfo["nickname"]} 님이 당신의 음원에 리뷰를 남겼습니다.",
            "http://localhost:8000/api/reviews/${reviewDetailsDTO.id}"
        )
        sendMessageToKafkaProducer(record)
        return APIResponse.of("Review created", reviewDetailsDTO.id)
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun lookupDetailsOfReview(@PathVariable id: String): APIResponse {
        val result = reviewService.lookupDetailsOfReviewById(id.toLong())
        return APIResponse.of("Review found", result)
    }

    @PostMapping("/found")
    @ResponseStatus(HttpStatus.CREATED)
    fun lookupReviewsInMusic(
        @HeaderSubject sub: String,
        @RequestParam musicId: String,
        @RequestParam(required = false, defaultValue = "likes") sort: String,
        @RequestBody dto: ReviewCursorRequestDTO,
        @PageableDefault(size = 20) pageable: Pageable
    ): APIResponse {
        val result = reviewService.findReviewListByMusicId(musicId.toLong(), sub.toLong(), sort, dto, pageable)
        return APIResponse.of("Reviews found", result)
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    fun updateReview(
        @HeaderSubject sub: String,
        @PathVariable("id") id: String,
        @Valid @RequestBody dto: ReviewUpdateDTO
    ): APIResponse {
        val reviewId = reviewService.updateReviewMessageAndScore(id.toLong(), sub.toLong(), dto)
        return APIResponse.of("Review updated", reviewId)
    }

    @PostMapping("/{id}/likes")
    @ResponseStatus(HttpStatus.CREATED)
    fun updateLikesOnReview(@HeaderSubject sub: String, @PathVariable("id") id: String): APIResponse {
        reviewService.changeLikesFlag(id.toLong(), sub.toLong())?.let {
            val myInfo = reviewService.getUserInformationOnRedis(sub.toLong())
            val record = KafkaRecordDTO(
                "review-like-topic",
                it.toString(),
                "${myInfo["nickname"] as String} 님이 당신의 리뷰에 좋아요를 눌렀습니다.",
                null
            )
            sendMessageToKafkaProducer(record)
        }
        return APIResponse.of("Likes of Review Changed")
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteReview(@HeaderSubject sub: String, @PathVariable("id") id: String): APIResponse {
        val reviewId = reviewService.deleteReview(id.toLong(), sub.toLong())
        reviewAsyncService.deleteReviewInElasticSearch(reviewId)
        return APIResponse.of("Review deleted")
    }

    @KafkaListener(groupId = "review-service-group", topics = ["user-deletion-topic"])
    fun deleteReviewsByUserWithdraw(@Payload message: String) {
        val obj = mapper.readValue(message, Map::class.java)
        val userId = obj["userId"].toString()
        reviewService.deleteReviewsByUserWithdraw(userId)
        reviewAsyncService.deleteReviewsByUserIdInElasticSearch(userId.toLong())
    }

    private fun sendMessageToKafkaProducer(record: KafkaRecordDTO) =
        post(
            url = "http://localhost:9000/api/kafka",
            headers = mapOf("Content-Type" to "application/json"),
            data = mapper.writeValueAsString(record)
        )

    companion object {
        private val mapper = jacksonObjectMapper()
    }
}