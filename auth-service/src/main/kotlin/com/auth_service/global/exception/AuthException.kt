package com.auth_service.global.exception

sealed class AuthException(
    val errorCode: ErrorCode,
    override val message: String
): RuntimeException(message) {

    class UserNotFoundException(message: String): AuthException(ErrorCode.NOT_FOUND, message)
    class PasswordNotFoundException(message: String): AuthException(ErrorCode.NOT_FOUND, message)
    class PasswordInvalidException(message: String): AuthException(ErrorCode.BAD_REQUEST, message)
    class TokenNotFoundException(message: String): AuthException(ErrorCode.NOT_FOUND, message)
    class TokenExpiredException(message: String): AuthException(ErrorCode.UNAUTHORIZED, message)
    class TokenVerifyFailException(message: String): AuthException(ErrorCode.UNAUTHORIZED, message)
}