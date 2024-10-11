package com.sound_bind.pay_service.global.exception

import com.sound_bind.global.dto.ErrorResponse
import com.sound_bind.global.utils.logger
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class PayServiceExceptionHandler {

    private val log = logger()

    @ExceptionHandler(PayServiceException::class)
    fun handlePayServiceException(e: PayServiceException): ResponseEntity<ErrorResponse> {
        log.error(e.message)
        return ResponseEntity
            .status(e.errorCode.status)
            .body(ErrorResponse.of(e.errorCode, e.message))
    }
}