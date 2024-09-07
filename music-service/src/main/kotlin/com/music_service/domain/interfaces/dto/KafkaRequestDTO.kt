package com.music_service.domain.interfaces.dto

data class KafkaRequestDTO(
    val topic: String,
    val message: KafkaMessage
) {
    data class KafkaNotificationDTO(
        val userId: Long,
        val content: String,
        val link: String?
    ): KafkaMessage

    sealed interface KafkaMessage
}