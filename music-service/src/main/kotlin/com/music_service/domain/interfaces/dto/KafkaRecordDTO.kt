package com.music_service.domain.interfaces.dto

data class KafkaRecordDTO private constructor(
    val topic: String,
    val message: KafkaMessageDTO
) {
    constructor(topic: String, userId: String, content: String, link: String?): this(
        topic,
        KafkaMessageDTO(userId, content, link)
    )

    data class KafkaMessageDTO(
        val userId: String,
        val content: String,
        val link: String?
    )
}