package com.sound_bind.review_service.domain.application.dto.message

data class MusicNotFoundMessageDTO(
    val musicId: Long,
    val reviewId: Long,
    val reviewerId: Long
)