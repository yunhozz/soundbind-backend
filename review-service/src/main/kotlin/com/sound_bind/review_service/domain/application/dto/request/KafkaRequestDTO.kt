package com.sound_bind.review_service.domain.application.dto.request

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
        val nickname: String,
        val oldScore: Double?,
        val score: Double
    ): KafkaMessage

    data class KafkaMusicReviewUpdateDTO(
        val musicId: Long,
        val oldScore: Double?,
        val score: Double
    ): KafkaMessage

    sealed interface KafkaMessage
}