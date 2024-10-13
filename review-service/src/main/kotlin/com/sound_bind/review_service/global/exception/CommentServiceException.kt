package com.sound_bind.review_service.global.exception

import com.sound_bind.global.exception.ErrorCode

sealed class CommentServiceException(
    val errorCode: ErrorCode,
    override val message: String
): RuntimeException(message) {

    class CommentNotFoundException(override val message: String): CommentServiceException(ErrorCode.NOT_FOUND, message)
    class CommentUpdateNotAuthorizedException(override val message: String): CommentServiceException(ErrorCode.UNAUTHORIZED, message)
}