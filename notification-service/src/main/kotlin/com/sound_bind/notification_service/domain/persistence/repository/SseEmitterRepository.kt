package com.sound_bind.notification_service.domain.persistence.repository

import com.sound_bind.notification_service.domain.persistence.entity.Notification
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

interface SseEmitterRepository {
    fun saveEmitter(emitterId: String, emitter: SseEmitter)
    fun saveNotification(eventId: String, notification: Notification)
    fun findEmittersByUserId(userId: String): Map<String, SseEmitter>
    fun findNotificationsByUserId(userId: String): Map<String, Notification>
    fun deleteEmitterById(emitterId: String): Boolean
    fun deleteNotificationById(eventId: String): Boolean
}