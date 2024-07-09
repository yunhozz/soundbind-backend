package com.sound_bind.review_service.domain.interfaces.handler

import com.review_service.domain.interfaces.dto.ErrorResponse
import com.review_service.domain.interfaces.dto.ErrorResponse.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class CommentServiceExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(CommentServiceExceptionHandler::class.java)
    }

    @ExceptionHandler(CommentServiceException::class)
    fun handleCommentServiceException(e: CommentServiceException): ResponseEntity<ErrorResponse> {
        log.error(e.message)
        return ResponseEntity
            .status(e.errorCode.status)
            .body(ErrorResponse.of(e.errorCode, e.message))
    }
}

sealed class CommentServiceException(
    val errorCode: ErrorCode,
    override val message: String
): RuntimeException(message) {

    class CommentNotFoundException(override val message: String): CommentServiceException(ErrorCode.NOT_FOUND, message)
}