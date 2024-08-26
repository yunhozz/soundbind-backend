package com.sound_bind.notification_service.domain.interfaces.handler

import com.sound_bind.notification_service.domain.interfaces.dto.ErrorResponse
import com.sound_bind.notification_service.global.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(e: RuntimeException): ResponseEntity<ErrorResponse> {
        log.error("[Runtime Error] ${e.localizedMessage}", e)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, e.localizedMessage))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        log.error(e.localizedMessage)
        return ResponseEntity
            .status(e.statusCode)
            .body(ErrorResponse.of(ErrorCode.BAD_REQUEST, e.bindingResult))
    }

    @ExceptionHandler(HttpClientErrorException::class)
    fun handleHttpClientErrorException(e: HttpClientErrorException): ResponseEntity<ErrorResponse> {
        log.error(e.localizedMessage)
        return ResponseEntity
            .status(e.statusCode)
            .body(ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED, e.responseBodyAsString))
    }
}