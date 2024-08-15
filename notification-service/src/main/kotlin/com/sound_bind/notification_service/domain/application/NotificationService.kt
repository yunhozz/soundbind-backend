package com.sound_bind.notification_service.domain.application

import com.sound_bind.notification_service.domain.application.dto.response.NotificationResponseDTO
import com.sound_bind.notification_service.domain.persistence.entity.Notification
import com.sound_bind.notification_service.domain.persistence.repository.NotificationRepository
import com.sound_bind.notification_service.domain.persistence.repository.SseEmitterRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val emitterRepository: SseEmitterRepository
) {

    companion object {
        private val log = LoggerFactory.getLogger(NotificationService::class.java)
        private const val EMITTER_TIMEOUT = 60 * 60 * 1000L // 1 hour
    }

    fun subscribe(userId: String, lastEventId: String?): SseEmitter {
        val emitterId = userId + "_" + System.currentTimeMillis()
        val emitter = SseEmitter(EMITTER_TIMEOUT)
        emitterRepository.saveEmitter(emitterId, emitter)

        emitter.onCompletion { complete(emitterId) }
        emitter.onTimeout { complete(emitterId) }

        sendToClient(emitter, emitterId, "[Event Stream Created] user id = $userId")

        lastEventId?.let { eventId ->
            emitterRepository.findNotificationsByUserId(userId).entries
                .filter { it.key > eventId }
                .forEach { sendToClient(emitter, it.key, it.value) }
        }

        return emitter
    }

    @Transactional
    @KafkaListener(
        groupId = "notification-service-group",
        topics = ["music-like-topic", "review-like-topic", "review-added-topic", "comment-added-topic"],
    )
    fun sendMessage(userId: String, @Payload message: String): String {
        val notification = Notification.create(userId, message)
        emitterRepository.findEmittersByUserId(userId)
            .forEach { (emitterId, emitter) ->
                emitterRepository.saveNotification(emitterId, notification)
                sendToClient(emitter, userId, NotificationResponseDTO(notification))
            }

        notificationRepository.save(notification)
        return notification.id!!
    }

    @Transactional
    fun checkNotificationById(id: String): NotificationResponseDTO {
        val notification = notificationRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Notification with id $id does not exist") }
        notification.check()
        return NotificationResponseDTO(notification)
    }

    @Transactional(readOnly = true)
    fun lookUpPageOfNotifications(receiverId: String, pageable: Pageable): Page<Notification> =
        notificationRepository.findSimpleNotificationsByReceiverId(receiverId, pageable)

    @Transactional
    fun deleteNotificationById(id: String) = notificationRepository.deleteById(id)

    private fun complete(emitterId: String) {
        log.info("Emitter Completed")
        emitterRepository.deleteEmitterById(emitterId)
    }

    private fun sendToClient(emitter: SseEmitter, emitterId: String, data: Any) =
        try {
            val event = SseEmitter.event()
                .id(emitterId)
                .name("sse")
                .data(data)
                .build()
            emitter.send(event)
            log.info("Data send to $emitterId")

        } catch (e: IOException) {
            log.error("Fail to send data", e)
            emitterRepository.deleteEmitterById(emitterId)
            throw IllegalArgumentException(e.localizedMessage)
        }
}