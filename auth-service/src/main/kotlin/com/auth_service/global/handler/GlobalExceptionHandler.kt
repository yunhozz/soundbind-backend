package com.auth_service.global.handler

import com.auth_service.global.exception.ErrorCode
import io.jsonwebtoken.JwtException
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
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

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(e: RuntimeException): ResponseEntity<ErrorResponse> {
        log.error(e.localizedMessage, e)
        return when (e) {
            is HttpClientErrorException ->
                ResponseEntity
                    .status(e.statusCode)
                    .body(ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED, e.responseBodyAsString))
            is DataIntegrityViolationException ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of(ErrorCode.BAD_REQUEST, e.localizedMessage))
            is JwtException ->
                ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.of(ErrorCode.UNAUTHORIZED, e.localizedMessage))
            else ->
                ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.of(ErrorCode.UNDEFINED_ERROR, e.localizedMessage))
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        log.error(e.localizedMessage)
        return ResponseEntity
            .status(e.statusCode)
            .body(ErrorResponse.of(ErrorCode.BAD_REQUEST, e.bindingResult))
    }
}