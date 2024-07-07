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
class ReviewServiceExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(ReviewServiceExceptionHandler::class.java)
    }

    @ExceptionHandler(ReviewServiceException::class)
    fun handleReviewServiceException(e: ReviewServiceException): ResponseEntity<ErrorResponse> {
        log.error(e.message)
        return ResponseEntity
            .status(e.errorCode.status)
            .body(ErrorResponse.of(e.errorCode, e.message))
    }
}

sealed class ReviewServiceException(
    val errorCode: ErrorCode,
    override val message: String
): RuntimeException(message) {

    class ReviewNotFoundException(override val message: String): ReviewServiceException(ErrorCode.NOT_FOUND, message)
    class ReviewAlreadyExistException(override val message: String): ReviewServiceException(ErrorCode.BAD_REQUEST, message)
}