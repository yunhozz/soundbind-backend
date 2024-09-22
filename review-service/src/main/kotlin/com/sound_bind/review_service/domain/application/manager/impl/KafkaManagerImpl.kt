package com.sound_bind.review_service.domain.application.manager.impl

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sound_bind.review_service.domain.application.manager.KafkaManager
import khttp.post
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class KafkaManagerImpl: KafkaManager {

    companion object {
        private val mapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        private const val MUSIC_REVIEW_TOPIC = "music-review-topic"
        private const val REVIEW_LIKE_TOPIC = "review-like-topic"
        private const val COMMENT_ADDED_TOPIC = "comment-added-topic"
        private const val MUSIC_NOT_FOUND_TOPIC = "music-not-found-topic"
    }

    @Value("\${uris.kafka-server-uri:http://localhost:9000}/api/kafka")
    private lateinit var kafkaRequestUri: String

    @Value("\${uris.music-service-uri:http://localhost:8070}/api/musics")
    private lateinit var musicServiceUri: String

    override fun sendMusicReviewCreateTopic(
        musicId: Long,
        reviewId: Long,
        reviewerId: Long,
        nickname: String,
        oldScore: Double?,
        score: Double
    ) {
        val musicReviewTopic = KafkaRequestDTO(
            topic = MUSIC_REVIEW_TOPIC,
            message = KafkaRequestDTO.KafkaMusicReviewCreateDTO(musicId, reviewId, reviewerId, nickname, oldScore, score)
        )
        sendMessageToKafkaProducer(musicReviewTopic)
    }

    override fun sendMusicReviewUpdateTopic(
        musicId: Long,
        reviewId: Long,
        reviewerId: Long,
        oldScore: Double?,
        score: Double
    ) {
        val musicReviewTopic = KafkaRequestDTO(
            topic = MUSIC_REVIEW_TOPIC,
            message = KafkaRequestDTO.KafkaMusicReviewUpdateDTO(musicId, reviewId, reviewerId, oldScore, score)
        )
        sendMessageToKafkaProducer(musicReviewTopic)
    }

    override fun sendReviewLikeTopic(userId: Long, content: String, link: String?) {
        val reviewLikeTopic = KafkaRequestDTO(
            topic = REVIEW_LIKE_TOPIC,
            message = KafkaRequestDTO.KafkaNotificationDTO(userId, content, link)
        )
        sendMessageToKafkaProducer(reviewLikeTopic)
    }

    override fun sendCommentAddedTopic(userId: Long, content: String, link: String?) {
        val commentAddedTopic = KafkaRequestDTO(
            topic = COMMENT_ADDED_TOPIC,
            message = KafkaRequestDTO.KafkaNotificationDTO(userId, content, link)
        )
        sendMessageToKafkaProducer(commentAddedTopic)
    }

    override fun sendMusicNotFoundTopic(userId: Long, content: String, link: String?) {
        val musicNotFoundTopic = KafkaRequestDTO(
            topic = MUSIC_NOT_FOUND_TOPIC,
            message = KafkaRequestDTO.KafkaNotificationDTO(userId, content, link)
        )
        sendMessageToKafkaProducer(musicNotFoundTopic)
    }

    private fun sendMessageToKafkaProducer(vararg message: KafkaRequestDTO) =
        post(
            url = kafkaRequestUri,
            headers = mapOf("Content-Type" to "application/json"),
            data = mapper.writeValueAsString(message.toList())
        )

    data class KafkaRequestDTO(
        val topic: String,
        val message: KafkaMessage
    ) {
        data class KafkaNotificationDTO(
            val userId: Long,
            val content: String,
            val link: String?
        ): KafkaMessage

        data class KafkaMusicReviewCreateDTO(
            val musicId: Long,
            val reviewId: Long,
            val reviewerId: Long,
            val nickname: String,
            val oldScore: Double?,
            val score: Double
        ): KafkaMessage

        data class KafkaMusicReviewUpdateDTO(
            val musicId: Long,
            val reviewId: Long,
            val reviewerId: Long,
            val oldScore: Double?,
            val score: Double
        ): KafkaMessage

        sealed interface KafkaMessage
    }
}