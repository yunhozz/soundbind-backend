package com.sound_bind.global.exception

import com.sound_bind.global.utils.logger
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
class GlobalExceptionHandler {

    private val log = logger()

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error(e.stackTraceToString())
        return ResponseEntity
            .status(500)
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