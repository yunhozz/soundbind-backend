package com.sound_bind.api_gateway.handler.exception

sealed class BusinessException(
    val errorCode: ErrorCode,
    override val message: String
): RuntimeException(message) {
    class TokenNotFoundOnCookieException(override val message: String): BusinessException(ErrorCode.NOT_FOUND, message)
    class TokenRefreshFailException(override val message: String): BusinessException(ErrorCode.UNAUTHORIZED, message)
    class InvalidApproachException(override val message: String): BusinessException(ErrorCode.FORBIDDEN, message)
    class NoPermissionException(override val message: String): BusinessException(ErrorCode.UNAUTHORIZED, message)
}