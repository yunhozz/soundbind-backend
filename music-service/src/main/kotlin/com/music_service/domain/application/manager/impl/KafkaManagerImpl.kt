package com.music_service.domain.application.manager.impl

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.music_service.domain.application.manager.KafkaManager
import khttp.post
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class KafkaManagerImpl: KafkaManager {

    companion object {
        private val mapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        const val MUSIC_SERVICE_GROUP = "music-service-group"
        const val MUSIC_REVIEW_TOPIC = "music-review-topic"
        const val USER_DELETION_TOPIC = "user-deletion-topic"
        private const val MUSIC_LIKE_TOPIC = "music-like-topic"
        private const val REVIEW_ADDED_TOPIC = "review-added-topic"
        private const val REVIEW_ROLLBACK_TOPIC = "review-rollback-topic"
    }

    @Value("\${uris.kafka-server-uri:http://localhost:9000}/api/kafka")
    private lateinit var kafkaRequestUri: String

    override fun sendMusicLikeTopic(userId: Long, content: String) {
        val musicLikeTopic = KafkaRequestDTO(
            topic = MUSIC_LIKE_TOPIC,
            message = KafkaRequestDTO.KafkaNotificationDTO(userId, content, link = null)
        )
        sendMessageToKafkaProducer(musicLikeTopic)
    }

    override fun sendReviewAddedTopic(userId: Long, content: String, link: String) {
        val reviewAddedTopic = KafkaRequestDTO(
            topic = REVIEW_ADDED_TOPIC,
            message = KafkaRequestDTO.KafkaNotificationDTO(userId, content, link)
        )
        sendMessageToKafkaProducer(reviewAddedTopic)
    }

    override fun sendReviewRollbackTopic(musicId: Long, reviewId: Long, reviewerId: Long) {
        val reviewRollbackTopic = KafkaRequestDTO(
            topic = REVIEW_ROLLBACK_TOPIC,
            message = KafkaRequestDTO.KafkaReviewRollbackDTO(musicId, reviewId, reviewerId)
        )
        sendMessageToKafkaProducer(reviewRollbackTopic)
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

        data class KafkaReviewRollbackDTO(
            val musicId: Long,
            val reviewId: Long,
            val reviewerId: Long
        ): KafkaMessage

        sealed interface KafkaMessage
    }
}