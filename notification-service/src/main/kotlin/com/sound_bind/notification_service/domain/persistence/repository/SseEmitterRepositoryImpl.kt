package com.sound_bind.notification_service.domain.persistence.repository

import com.sound_bind.notification_service.domain.persistence.entity.Notification
import org.springframework.stereotype.Repository
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

@Repository
class SseEmitterRepositoryImpl: SseEmitterRepository {
    private val emitters = ConcurrentHashMap<String, SseEmitter>()
    private val notifications = ConcurrentHashMap<String, Notification>()

    override fun saveEmitter(emitterId: String, emitter: SseEmitter) {
        emitters[emitterId] = emitter
    }

    override fun saveNotification(eventId: String, notification: Notification) {
        notifications[eventId] = notification
    }

    override fun findEmittersByUserId(userId: String): Map<String, SseEmitter> =
        emitters.entries
            .filter { it.key.startsWith(userId) }
            .associate { it.key to it.value }

    override fun findNotificationsByUserId(userId: String): Map<String, Notification> =
        notifications.entries
            .filter { it.key.startsWith(userId) }
            .associate { it.key to it.value }

    override fun deleteEmitterById(emitterId: String): Boolean =
        emitters.keys.removeIf { it == emitterId }

    override fun deleteNotificationById(eventId: String): Boolean =
        notifications.keys.removeIf { it == eventId }
}