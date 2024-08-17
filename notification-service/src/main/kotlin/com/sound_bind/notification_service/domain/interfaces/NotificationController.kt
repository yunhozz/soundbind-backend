package com.sound_bind.notification_service.domain.interfaces

import com.sound_bind.notification_service.domain.application.NotificationService
import com.sound_bind.notification_service.domain.persistence.entity.Notification
import com.sound_bind.notification_service.global.annotation.HeaderSubject
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/notifications")
class NotificationController(private val notificationService: NotificationService) {

    @GetMapping(value = ["/subscribe"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun subscribe(
        @RequestHeader(value = "Last-Event-ID", required = false) lastEventId: String?,
        @HeaderSubject sub: String
    ): ResponseEntity<SseEmitter> {
        val emitter = notificationService.subscribe(sub, lastEventId)
        return ResponseEntity.ok(emitter)
    }

    @GetMapping
    fun lookUpPageOfNotifications(
        @HeaderSubject sub: String,
        @RequestParam(required = false, defaultValue = "0") page: String
    ): ResponseEntity<Page<Notification>> {
        val pageable = PageRequest.of(page.toInt(), 10)
        val result = notificationService.lookUpPageOfNotifications(sub, pageable)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/{id}")
    fun checkNotification(@PathVariable id: String): ResponseEntity<String> {
        notificationService.checkNotificationById(id)
        return ResponseEntity.ok("Notification check success")
    }

    @PostMapping
    fun checkNotificationsInPage(
        @HeaderSubject sub: String,
        @RequestParam(required = false, defaultValue = "0") page: String
    ): ResponseEntity<String> {
        notificationService.checkNotificationsInPage(sub, page.toInt())
        return ResponseEntity.ok("Notifications check success")
    }

    @DeleteMapping("/{id}")
    fun deleteNotification(@PathVariable id: String): ResponseEntity<Void> {
        notificationService.deleteNotificationById(id)
        return ResponseEntity.noContent().build()
    }
}