package com.auth_service.global.exception

sealed class UserManageException(
    val errorCode: ErrorCode,
    override val message: String
): RuntimeException(message) {

    class UserNotFoundException(message: String): UserManageException(ErrorCode.NOT_FOUND, message)
    class EmailDuplicateException(message: String): UserManageException(ErrorCode.BAD_REQUEST, message)
    class UserPasswordNotFoundException(message: String): UserManageException(ErrorCode.NOT_FOUND, message)
    class UserProfileNotFoundException(message: String): UserManageException(ErrorCode.NOT_FOUND, message)
}