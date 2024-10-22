package com.music_service.domain.application.manager.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.music_service.domain.application.manager.KafkaManager
import com.sound_bind.global.dto.KafkaEvent
import com.sound_bind.global.dto.KafkaMessage
import com.sound_bind.global.utils.KafkaConstants
import khttp.post
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class KafkaManagerImpl: KafkaManager {

    private val mapper = jacksonObjectMapper()

    @Value("\${uris.kafka-server-uri:http://localhost:9000}/kafka")
    private lateinit var kafkaRequestUri: String

    override fun sendMusicLikeTopic(userId: Long, content: String) {
        val musicLikeEvent = KafkaEvent(
            topic = KafkaConstants.MUSIC_LIKE_TOPIC,
            message = KafkaMessage.NotificationMessage(userId, content, link = null)
        )
        sendEventsToKafkaProducer(musicLikeEvent)
    }

    override fun sendReviewAddedTopic(userId: Long, content: String, link: String) {
        val reviewAddedEvent = KafkaEvent(
            topic = KafkaConstants.REVIEW_ADDED_TOPIC,
            message = KafkaMessage.NotificationMessage(userId, content, link)
        )
        sendEventsToKafkaProducer(reviewAddedEvent)
    }

    override fun sendReviewRollbackTopic(musicId: Long, reviewId: Long, reviewerId: Long) {
        val reviewRollbackEvent = KafkaEvent(
            topic = KafkaConstants.REVIEW_ROLLBACK_TOPIC,
            message = KafkaMessage.ReviewRollbackMessage(musicId, reviewId, reviewerId)
        )
        sendEventsToKafkaProducer(reviewRollbackEvent)
    }

    private fun sendEventsToKafkaProducer(vararg events: KafkaEvent) =
        post(
            url = kafkaRequestUri,
            headers = mapOf("Content-Type" to "application/json"),
            data = mapper.writeValueAsString(events.toList())
        )
}