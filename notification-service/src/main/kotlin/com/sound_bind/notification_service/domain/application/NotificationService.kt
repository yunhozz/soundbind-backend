package com.sound_bind.notification_service.domain.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sound_bind.notification_service.domain.persistence.entity.Notification
import com.sound_bind.notification_service.domain.persistence.repository.NotificationRepository
import com.sound_bind.notification_service.domain.persistence.repository.SseEmitterRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
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

        emitter.onCompletion { emitterRepository.deleteEmitterById(emitterId) }
        emitter.onTimeout { emitter.complete() }

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
    fun sendMessage(@Payload message: String): String {
        val obj = jacksonObjectMapper().readValue(message, Map::class.java)
        val userId = obj["userId"] as String
        val content = obj["content"] as String
        val link = obj["link"] as? String

        val notification = Notification.create(userId, content, link)
        emitterRepository.findEmittersByUserId(userId)
            .forEach { (emitterId, emitter) ->
                emitterRepository.saveNotification(emitterId, notification)
                sendToClient(emitter, emitterId, notification.message)
            }

        notificationRepository.save(notification)
        return notification.id!!
    }

    @Transactional
    fun checkNotificationById(id: String) {
        val notification = notificationRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Notification with id $id does not exist") }
        notification.check()
        notificationRepository.save(notification)
    }

    @Transactional
    fun checkNotificationsInPage(userId: String, page: Int) {
        val pageable = PageRequest.of(page, 10)
        notificationRepository.findAndCheckNotificationsInPage(userId, pageable)
    }

    @Transactional(readOnly = true)
    fun lookUpPageOfNotifications(userId: String, pageable: Pageable): Page<Notification> =
        notificationRepository.findSimpleNotificationsByUserId(userId, pageable)

    @Transactional
    fun deleteNotificationById(id: String) = notificationRepository.deleteById(id)

    @Transactional
    fun deleteCheckedNotificationsInPage(userId: String, page: Int) {
        val pageable = PageRequest.of(page, 10)
        notificationRepository.deleteCheckedNotificationsInPage(userId, pageable)
    }

    private fun sendToClient(emitter: SseEmitter, emitterId: String, data: Any) =
        try {
            val event = SseEmitter.event()
                .id(emitterId)
                .name("sse")
                .data(data, MediaType.APPLICATION_JSON)
                .build()
            emitter.send(event)
            log.info("Data send to $emitterId")

        } catch (e: IOException) {
            log.error("Fail to send data", e)
            emitterRepository.deleteEmitterById(emitterId)
            emitter.completeWithError(e)
        }
}