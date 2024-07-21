package com.auth_service.domain.interfaces.handler

import com.auth_service.domain.interfaces.dto.ErrorResponse
import com.auth_service.domain.interfaces.dto.ErrorResponse.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class UserManageExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(UserManageExceptionHandler::class.java)
    }

    @ExceptionHandler(UserManageException::class)
    fun handleUserManageException(e: UserManageException): ResponseEntity<ErrorResponse> {
        log.error(e.message)
        return ResponseEntity
            .status(e.errorCode.status)
            .body(ErrorResponse.of(e.errorCode, e.message))
    }
}

sealed class UserManageException(
    val errorCode: ErrorCode,
    override val message: String
): RuntimeException(message) {

    class UserNotFoundException(override val message: String): UserManageException(ErrorCode.NOT_FOUND, message)
    class EmailDuplicateException(override val message: String): UserManageException(ErrorCode.BAD_REQUEST, message)
}