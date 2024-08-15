package com.sound_bind.notification_service.domain.application.dto.response

import com.sound_bind.notification_service.domain.persistence.entity.Notification
import java.time.LocalDateTime

data class NotificationResponseDTO(private val notification: Notification) {
    val id: String = notification.id!!
    val userId: String = notification.userId
    val message: String = notification.message
    val isChecked: Boolean = notification.isChecked
    val createdAt: LocalDateTime? = notification.createdAt
}
