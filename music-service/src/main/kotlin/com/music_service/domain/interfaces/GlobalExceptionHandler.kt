package com.music_service.domain.interfaces

import com.music_service.global.dto.response.ErrorResponse
import com.music_service.domain.interfaces.dto.ErrorResponse
import com.music_service.domain.interfaces.dto.ErrorResponse.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(RuntimeException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleRuntimeException(e: RuntimeException): ErrorResponse {
        log.error(e.stackTraceToString())
        return ErrorResponse.of(ErrorResponse.ErrorCode.INTERNAL_SERVER_ERROR, e.localizedMessage)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ErrorResponse {
        log.error(e.localizedMessage)
        return ErrorResponse.of(ErrorResponse.ErrorCode.INVALID_REQUEST, e.bindingResult)
    }

    @ExceptionHandler(HttpClientErrorException::class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    fun handleHttpClientErrorException(e: HttpClientErrorException): ErrorResponse {
        log.error(e.localizedMessage)
        return ErrorResponse.of(ErrorResponse.ErrorCode.METHOD_NOT_ALLOWED, e.responseBodyAsString)
    }
}