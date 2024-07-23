package com.auth_service.domain.interfaces.handler

import com.auth_service.domain.interfaces.dto.ErrorResponse
import com.auth_service.global.exception.ErrorCode
import org.slf4j.LoggerFactory
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

    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

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