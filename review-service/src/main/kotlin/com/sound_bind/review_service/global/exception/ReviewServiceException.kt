package com.sound_bind.review_service.global.exception

sealed class ReviewServiceException(
    val errorCode: ErrorCode,
    override val message: String
): RuntimeException(message) {

    class ReviewNotFoundException(override val message: String): ReviewServiceException(ErrorCode.NOT_FOUND, message)
    class ReviewAlreadyExistException(override val message: String): ReviewServiceException(ErrorCode.BAD_REQUEST, message)
    class ReviewNotUpdatableException(override val message: String): ReviewServiceException(ErrorCode.BAD_REQUEST, message)
    class ReviewUpdateNotAuthorizedException(override val message: String): ReviewServiceException(ErrorCode.UNAUTHORIZED, message)
    class NegativeValueException(override val message: String): ReviewServiceException(ErrorCode.BAD_REQUEST, message)
}