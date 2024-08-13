package com.sound_bind.review_service.global.exception

sealed class CommentServiceException(
    val errorCode: ErrorCode,
    override val message: String
): RuntimeException(message) {

    class CommentNotFoundException(override val message: String): CommentServiceException(ErrorCode.NOT_FOUND, message)
    class CommentUpdateNotAuthorizedException(override val message: String): CommentServiceException(ErrorCode.UNAUTHORIZED, message)
}