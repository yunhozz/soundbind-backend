package com.sound_bind.review_service.domain.interfaces

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.review_service.domain.interfaces.dto.APIResponse
import com.sound_bind.review_service.domain.application.ElasticsearchService
import com.sound_bind.review_service.domain.application.ReviewService
import com.sound_bind.review_service.domain.application.dto.request.ReviewCreateDTO
import com.sound_bind.review_service.domain.application.dto.request.ReviewUpdateDTO
import com.sound_bind.review_service.domain.interfaces.dto.KafkaRequestDTO
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewCursorDTO
import com.sound_bind.review_service.global.annotation.HeaderSubject
import com.sound_bind.review_service.global.enums.ReviewSort
import jakarta.validation.Valid
import khttp.get
import khttp.post
import org.springframework.data.domain.PageRequest
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
    private val elasticsearchService: ElasticsearchService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createReviewOnMusic(
        @HeaderSubject sub: String,
        @RequestParam musicId: String,
        @Valid @RequestBody dto: ReviewCreateDTO
    ): APIResponse {
        val response = get("http://localhost:8070/api/musics/$musicId/musician")
        if (response.statusCode != HttpStatus.OK.value()) {
            val message = response.jsonObject.getString("message")
            return APIResponse.of(message)
        }
        val reviewId = reviewService.createReview(musicId.toLong(), sub.toLong(), dto)

        val obj = mapper.readValue(response.text, Map::class.java)
        val musicianId = mapper.writeValueAsString(obj["data"])
        val myInfo = reviewService.getUserInformationOnRedis(sub.toLong())

        val notificationRequest = KafkaRequestDTO(
            topic = "review-added-topic",
            message = KafkaRequestDTO.KafkaNotificationDTO(
                userId = musicianId.toLong(),
                content = "${myInfo["nickname"]} 님이 당신의 음원에 리뷰를 남겼습니다.",
                link = "http://localhost:8000/api/reviews/$reviewId"
            )
        )
        val musicScoreRequest = KafkaRequestDTO(
            topic = "review-score-topic",
            message = KafkaRequestDTO.KafkaMusicScoreDTO(
                musicId.toLong(),
                oldScore = null,
                score = dto.score
            )
        )
        sendMessageToKafkaProducer(listOf(notificationRequest, musicScoreRequest))
        return APIResponse.of("Review created", reviewId)
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun lookupDetailsOfReview(@PathVariable id: String): APIResponse {
        val result = reviewService.lookupDetailsOfReviewById(id.toLong())
        return APIResponse.of("Review found", result)
    }

    @GetMapping("/found")
    @ResponseStatus(HttpStatus.OK)
    fun lookUpReviewsInMusicByDefault(@HeaderSubject sub: String, @RequestParam musicId: String): APIResponse {
        val result = elasticsearchService.findReviewListByMusicIdV2(
            musicId.toLong(),
            userId = sub.toLong(),
            reviewSort = ReviewSort.LIKES,
            dto = null
        )
        return APIResponse.of("Reviews found", result)
    }

    @PostMapping("/found/v1")
    @ResponseStatus(HttpStatus.CREATED)
    fun lookupReviewsInMusicByConditionsV1(
        @HeaderSubject sub: String,
        @RequestParam musicId: String,
        @RequestParam(required = false, defaultValue = "likes") sort: String,
        @RequestParam(required = false, defaultValue = "0") page: String,
        @RequestBody(required = false) dto: ReviewCursorDTO,
    ): APIResponse {
        val result = reviewService.findReviewListByMusicIdV1(
            musicId.toLong(),
            userId = sub.toLong(),
            reviewSort = ReviewSort.of(sort),
            dto,
            pageable = PageRequest.of(page.toInt(), 20)
        )
        return APIResponse.of("Reviews found", result)
    }

    @PostMapping("/found/v2")
    @ResponseStatus(HttpStatus.CREATED)
    fun lookupReviewsInMusicByConditionsV2(
        @HeaderSubject sub: String,
        @RequestParam musicId: String,
        @RequestParam(required = false, defaultValue = "likes") sort: String,
        @RequestBody(required = false) dto: ReviewCursorDTO,
    ): APIResponse {
        val result = elasticsearchService.findReviewListByMusicIdV2(
            musicId.toLong(),
            userId = sub.toLong(),
            reviewSort = ReviewSort.of(sort),
            dto
        )
        return APIResponse.of("Reviews found", result)
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    fun updateReview(
        @HeaderSubject sub: String,
        @PathVariable("id") id: String,
        @Valid @RequestBody dto: ReviewUpdateDTO
    ): APIResponse {
        val reviewScoreDTO = reviewService.updateReviewMessageAndScore(id.toLong(), sub.toLong(), dto)
        val musicScoreRequest = KafkaRequestDTO(
            topic = "review-score-topic",
            message = KafkaRequestDTO.KafkaMusicScoreDTO(
                musicId = reviewScoreDTO.musicId,
                oldScore = reviewScoreDTO.oldScore,
                score = reviewScoreDTO.newScore
            )
        )
        sendMessageToKafkaProducer(listOf(musicScoreRequest))
        return APIResponse.of("Review updated", reviewScoreDTO.id)
    }

    @PostMapping("/{id}/likes")
    @ResponseStatus(HttpStatus.CREATED)
    fun updateLikesOnReview(@HeaderSubject sub: String, @PathVariable("id") id: String): APIResponse {
        reviewService.changeLikesFlag(id.toLong(), sub.toLong())?.let {
            val myInfo = reviewService.getUserInformationOnRedis(sub.toLong())
            val notificationRequest = KafkaRequestDTO(
                topic = "review-like-topic",
                message = KafkaRequestDTO.KafkaNotificationDTO(
                    userId = it,
                    content = "${myInfo["nickname"] as String} 님이 당신의 리뷰에 좋아요를 눌렀습니다.",
                    link = null
                )
            )
            sendMessageToKafkaProducer(listOf(notificationRequest))
        }
        return APIResponse.of("Likes of Review Changed")
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteReview(@HeaderSubject sub: String, @PathVariable("id") id: String): APIResponse {
        val reviewScoreDTO = reviewService.deleteReview(id.toLong(), sub.toLong())
        val musicScoreRequest = KafkaRequestDTO(
            topic = "review-score-topic",
            message = KafkaRequestDTO.KafkaMusicScoreDTO(
                musicId = reviewScoreDTO.musicId,
                oldScore = reviewScoreDTO.oldScore,
                score = reviewScoreDTO.newScore
            )
        )
        sendMessageToKafkaProducer(listOf(musicScoreRequest))
        return APIResponse.of("Review deleted")
    }

    companion object {
        private val mapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        private fun sendMessageToKafkaProducer(request: List<KafkaRequestDTO>) =
            post(
                url = "http://localhost:9000/api/kafka",
                headers = mapOf("Content-Type" to "application/json"),
                data = mapper.writeValueAsString(request)
            )
    }
}