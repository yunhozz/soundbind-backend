package com.sound_bind.pay_service.domain.application.dto.response

import java.time.LocalDateTime

data class SponsorResponseDTO(
    val id: Long,
    val senderId: Long,
    val receiverId: Long,
    val pointAmount: Int,
    val isCompleted: Boolean,
    val receivedTime: LocalDateTime
)