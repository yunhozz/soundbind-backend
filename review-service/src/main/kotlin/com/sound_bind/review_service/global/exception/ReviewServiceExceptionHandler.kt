package com.sound_bind.review_service.global.exception

import com.sound_bind.global.exception.ErrorResponse
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