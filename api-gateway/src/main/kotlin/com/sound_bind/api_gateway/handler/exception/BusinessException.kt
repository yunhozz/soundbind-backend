package com.sound_bind.api_gateway.handler.exception

open class BusinessException(
    val errorCode: ErrorCode,
    override val message: String
): RuntimeException(message) {

    class TokenNotFoundOnCookieException(override val message: String): BusinessException(ErrorCode.FRAME_WORK_INTERNAL_ERROR, message)
    class TokenRefreshFailException(override val message: String): BusinessException(ErrorCode.FRAME_WORK_INTERNAL_ERROR, message)
}