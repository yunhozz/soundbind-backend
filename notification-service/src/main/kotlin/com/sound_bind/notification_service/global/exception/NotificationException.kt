package com.sound_bind.notification_service.global.exception

import com.sound_bind.global.exception.ErrorCode

sealed class NotificationException(
    val errorCode: ErrorCode,
    override val message: String
): RuntimeException(message) {
    class NotificationNotFoundException(message: String): NotificationException(ErrorCode.NOT_FOUND, message)
    class SseSendFailException(message: String): NotificationException(ErrorCode.INTERNAL_SERVER_ERROR, message)
    class NotificationAlreadyCheckedException(message: String): NotificationException(ErrorCode.BAD_REQUEST, message)
}