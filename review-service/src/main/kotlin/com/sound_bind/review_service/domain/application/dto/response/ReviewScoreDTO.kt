package com.sound_bind.review_service.domain.application.dto.response

data class ReviewScoreDTO(
    val id: Long,
    val musicId: Long,
    val oldScore: Double?,
    val newScore: Double
)