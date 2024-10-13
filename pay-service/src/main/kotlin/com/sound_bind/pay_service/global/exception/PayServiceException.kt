package com.sound_bind.pay_service.global.exception

import com.sound_bind.global.exception.ErrorCode

sealed class PayServiceException(
    val errorCode: ErrorCode,
    override val message: String
): RuntimeException(message) {
    class PointNotFoundException(override val message: String): PayServiceException(ErrorCode.NOT_FOUND, message)
    class SponsorNotFoundException(override val message: String): PayServiceException(ErrorCode.NOT_FOUND, message)
}