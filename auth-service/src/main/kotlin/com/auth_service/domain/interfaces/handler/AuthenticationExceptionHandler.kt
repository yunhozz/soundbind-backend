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
class AuthenticationExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(UserManageExceptionHandler::class.java)
    }

    @ExceptionHandler(AuthException::class)
    fun handleUserManageException(e: AuthException): ResponseEntity<ErrorResponse> {
        log.error(e.message)
        return ResponseEntity
            .status(e.errorCode.status)
            .body(ErrorResponse.of(e.errorCode, e.message))
    }
}

sealed class AuthException(
    val errorCode: ErrorCode,
    override val message: String
): RuntimeException(message) {

    class UserNotFoundException(message: String): AuthException(ErrorCode.NOT_FOUND, message)
    class PasswordNotFoundException(message: String): AuthException(ErrorCode.NOT_FOUND, message)
    class PasswordInvalidException(message: String): AuthException(ErrorCode.BAD_REQUEST, message)
    class TokenNotFoundException(message: String): AuthException(ErrorCode.NOT_FOUND, message)
}