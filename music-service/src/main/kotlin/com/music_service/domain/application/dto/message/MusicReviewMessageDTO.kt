package com.music_service.domain.application.dto.message

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class MusicReviewMessageDTO(
    val musicId: Long,
    val reviewId: Long,
    val reviewerId: Long,
    val nickname: String?,
    val oldScore: Double?,
    val score: Double,
)