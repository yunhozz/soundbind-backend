package com.sound_bind.notification_service.global.exception

sealed class NotificationException(
    val errorCode: ErrorCode,
    override val message: String
): RuntimeException(message) {
    class NotificationNotFoundException(message: String): NotificationException(ErrorCode.NOT_FOUND, message)
    class SseSendFailException(message: String): NotificationException(ErrorCode.INTERNAL_SERVER_ERROR, message)
    class NotificationAlreadyCheckedException(message: String): NotificationException(ErrorCode.BAD_REQUEST, message)
}