package com.sound_bind.notification_service.domain.interfaces.handler

import com.sound_bind.notification_service.domain.interfaces.dto.ErrorResponse
import com.sound_bind.notification_service.global.exception.NotificationException
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class NotificationExceptionHandler {

    private val log = LoggerFactory.getLogger(NotificationExceptionHandler::class.java)

    @ExceptionHandler(NotificationException::class)
    fun handleUserManageException(e: NotificationException): ResponseEntity<ErrorResponse> {
        log.error(e.message)
        return ResponseEntity
            .status(e.errorCode.status)
            .body(ErrorResponse.of(e.errorCode, e.message))
    }
}