package com.sound_bind.notification_service.domain.interfaces

import com.sound_bind.notification_service.domain.application.NotificationService
import com.sound_bind.notification_service.domain.persistence.entity.Notification
import com.sound_bind.notification_service.global.annotation.HeaderSubject
import io.swagger.v3.oas.annotations.Operation
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
    @Operation(summary = "SSE 구독")
    fun subscribe(
        @RequestHeader(value = "Last-Event-ID", required = false) lastEventId: String?,
        @HeaderSubject sub: String
    ): ResponseEntity<SseEmitter> {
        val emitter = notificationService.subscribe(sub, lastEventId)
        return ResponseEntity.ok(emitter)
    }

    @GetMapping
    @Operation(summary = "알림 목록 페이징 조회")
    fun lookUpPageOfNotifications(
        @HeaderSubject sub: String,
        @RequestParam(required = false, defaultValue = "0") page: String
    ): ResponseEntity<Page<Notification>> {
        val pageable = PageRequest.of(page.toInt(), 10)
        val result = notificationService.lookUpPageOfNotifications(sub, pageable)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/{id}")
    @Operation(summary = "알림 확인 처리")
    fun checkNotification(@PathVariable id: String): ResponseEntity<String> {
        notificationService.checkNotificationById(id)
        return ResponseEntity.ok("Notification check success")
    }

    @PostMapping
    @Operation(summary = "페이지 내 알림 전체 확인 처리")
    fun checkNotificationsInPage(
        @HeaderSubject sub: String,
        @RequestParam(required = false, defaultValue = "0") page: String
    ): ResponseEntity<String> {
        notificationService.checkNotificationsInPage(sub, page.toInt())
        return ResponseEntity.ok("Notifications check success")
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "알림 삭제")
    fun deleteNotification(@PathVariable id: String): ResponseEntity<Void> {
        notificationService.deleteNotificationById(id)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping
    @Operation(summary = "페이지 내 확인된 알림 전체 삭제")
    fun deleteCheckedNotificationsInPage(
        @HeaderSubject sub: String,
        @RequestParam(required = false, defaultValue = "0") page: String
    ): ResponseEntity<Void> {
        notificationService.deleteCheckedNotificationsInPage(sub, page.toInt())
        return ResponseEntity.noContent().build()
    }
}