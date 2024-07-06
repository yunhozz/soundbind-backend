package com.music_service.domain.interfaces

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
    fun handleRuntimeException(e: RuntimeException): ResponseEntity<ErrorResponse> {
        log.error(e.stackTraceToString())
        return ResponseEntity
            .status(500)
            .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, e.localizedMessage))
    }

    @ExceptionHandler(MusicServiceException::class)
    fun handleMusicServiceException(e: MusicServiceException): ResponseEntity<ErrorResponse> {
        log.error(e.message)
        return ResponseEntity
            .status(e.errorCode.status)
            .body(ErrorResponse.of(e.errorCode, e.message))
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

sealed class MusicServiceException(
    val errorCode: ErrorCode,
    override val message: String
): RuntimeException(message) {

    class MusicNotFoundException(override val message: String): MusicServiceException(ErrorCode.NOT_FOUND, message)
    class MusicFileNotExistException(override val message: String): MusicServiceException(ErrorCode.NOT_FOUND, message)
}