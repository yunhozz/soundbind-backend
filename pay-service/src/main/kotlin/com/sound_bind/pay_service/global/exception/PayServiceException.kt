package com.sound_bind.pay_service.global.exception

sealed class PayServiceException(
    val errorCode: ErrorCode,
    override val message: String
): RuntimeException(message) {
    class PointNotFoundException(override val message: String): PayServiceException(ErrorCode.NOT_FOUND, message)
    class SponsorNotFoundException(override val message: String): PayServiceException(ErrorCode.NOT_FOUND, message)
}