package com.sound_bind.global.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

data class KafkaEvent(
    val topic: String,
    val message: KafkaMessage
)

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(KafkaMessage.MusicReviewCreateMessage::class, name = "MusicReviewCreateMessage")
)
sealed interface KafkaMessage {
    data class UserInfoMessage(
        val userId: Long
    ): KafkaMessage

    data class NotificationMessage(
        val userId: Long,
        val content: String,
        val link: String?
    ): KafkaMessage

    data class MusicReviewCreateMessage(
        val musicId: Long,
        val reviewId: Long,
        val reviewerId: Long,
        val nickname: String,
        val oldScore: Double?,
        val score: Double
    ): KafkaMessage

    data class MusicReviewUpdateMessage(
        val musicId: Long,
        val reviewId: Long,
        val reviewerId: Long,
        val oldScore: Double?,
        val score: Double
    ): KafkaMessage

    data class ReviewRollbackMessage(
        val musicId: Long,
        val reviewId: Long,
        val reviewerId: Long
    ): KafkaMessage
}