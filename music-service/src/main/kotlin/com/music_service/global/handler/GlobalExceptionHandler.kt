package com.music_service.global.handler

import com.music_service.global.exception.ErrorCode
import com.music_service.global.exception.MusicServiceException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
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

    data class ErrorResponse(
        val status: Int,
        val code: String,
        val message: String,
        val fieldErrors: List<FieldErrorResponse>
    ) {
        private constructor(errorCode: ErrorCode): this(status = errorCode.status, code = errorCode.code, message = errorCode.message, fieldErrors = emptyList())
        private constructor(errorCode: ErrorCode, message: String): this(status = errorCode.status, code = errorCode.code, message, fieldErrors = emptyList())
        private constructor(errorCode: ErrorCode, fieldErrors: List<FieldErrorResponse>): this(status = errorCode.status, code = errorCode.code, message = errorCode.message, fieldErrors)

        companion object {
            fun of(errorCode: ErrorCode): ErrorResponse = ErrorResponse(errorCode)
            fun of(errorCode: ErrorCode, message: String): ErrorResponse = ErrorResponse(errorCode, message)
            fun of(errorCode: ErrorCode, result: BindingResult): ErrorResponse = ErrorResponse(errorCode, FieldErrorResponse.of(result))
        }

        data class FieldErrorResponse(
            val field: String,
            val value: String,
            val reason: String?
        ) {
            companion object {
                fun of(field: String, value: String, reason: String): List<FieldErrorResponse> = listOf(
                    FieldErrorResponse(field, value, reason)
                )

                fun of(result: BindingResult): List<FieldErrorResponse> = result.fieldErrors.stream().map {
                    FieldErrorResponse(
                        field = it.field,
                        value = (it.rejectedValue ?: "").toString(),
                        reason = it.defaultMessage
                    )
                }.toList()
            }
        }
    }
}