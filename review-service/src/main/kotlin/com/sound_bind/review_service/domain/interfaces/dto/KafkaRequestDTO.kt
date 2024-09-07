package com.sound_bind.review_service.domain.interfaces.dto

data class KafkaRequestDTO(
    val topic: String,
    val message: KafkaMessage
) {
    data class KafkaNotificationDTO(
        val userId: Long,
        val content: String,
        val link: String?
    ): KafkaMessage

    data class KafkaMusicScoreDTO(
        val musicId: Long,
        val oldScore: Double?,
        val score: Double
    ): KafkaMessage

    sealed interface KafkaMessage
}