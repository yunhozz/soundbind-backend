package com.sound_bind.review_service.domain.application.manager.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sound_bind.global.dto.KafkaEvent
import com.sound_bind.global.dto.KafkaMessage
import com.sound_bind.global.utils.KafkaConstants
import com.sound_bind.review_service.domain.application.manager.KafkaManager
import khttp.post
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class KafkaManagerImpl: KafkaManager {

    private val mapper = jacksonObjectMapper()

    @Value("\${uris.kafka-server-uri:http://localhost:9000}/kafka")
    private lateinit var kafkaRequestUri: String

    override fun sendMusicReviewCreateTopic(
        musicId: Long,
        reviewId: Long,
        reviewerId: Long,
        nickname: String,
        oldScore: Double?,
        score: Double
    ) {
        val musicReviewEvent = KafkaEvent(
            topic = KafkaConstants.MUSIC_REVIEW_TOPIC,
            message = KafkaMessage.MusicReviewCreateMessage(musicId, reviewId, reviewerId, nickname, oldScore, score)
        )
        sendEventsToKafkaProducer(musicReviewEvent)
    }

    override fun sendMusicReviewUpdateTopic(
        musicId: Long,
        reviewId: Long,
        reviewerId: Long,
        oldScore: Double?,
        score: Double
    ) {
        val musicReviewEvent = KafkaEvent(
            topic = KafkaConstants.MUSIC_REVIEW_TOPIC,
            message = KafkaMessage.MusicReviewUpdateMessage(musicId, reviewId, reviewerId, oldScore, score)
        )
        sendEventsToKafkaProducer(musicReviewEvent)
    }

    override fun sendReviewLikeTopic(userId: Long, content: String, link: String?) {
        val reviewLikeEvent = KafkaEvent(
            topic = KafkaConstants.REVIEW_LIKE_TOPIC,
            message = KafkaMessage.NotificationMessage(userId, content, link)
        )
        sendEventsToKafkaProducer(reviewLikeEvent)
    }

    override fun sendCommentAddedTopic(userId: Long, content: String, link: String?) {
        val commentAddedEvent = KafkaEvent(
            topic = KafkaConstants.COMMENT_ADDED_TOPIC,
            message = KafkaMessage.NotificationMessage(userId, content, link)
        )
        sendEventsToKafkaProducer(commentAddedEvent)
    }

    override fun sendMusicNotFoundTopic(userId: Long, content: String, link: String?) {
        val musicNotFoundEvent = KafkaEvent(
            topic = KafkaConstants.MUSIC_NOT_FOUND_TOPIC,
            message = KafkaMessage.NotificationMessage(userId, content, link)
        )
        sendEventsToKafkaProducer(musicNotFoundEvent)
    }

    private fun sendEventsToKafkaProducer(vararg events: KafkaEvent) =
        post(
            url = kafkaRequestUri,
            headers = mapOf("Content-Type" to "application/json"),
            data = mapper.writeValueAsString(events.toList())
        )
}