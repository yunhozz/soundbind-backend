package com.music_service.domain.application.dto.message

data class ReviewScoreMessageDTO(
    val musicId: Long,
    val score: Double,
    val oldScore: Double?
)