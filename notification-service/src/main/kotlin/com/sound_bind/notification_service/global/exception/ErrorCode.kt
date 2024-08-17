package com.sound_bind.notification_service.global.exception

enum class ErrorCode(
    val status: Int,
    val code: String,
    val message: String
) {
    BAD_REQUEST(400, "N-000", "Bad Request"),
    UNAUTHORIZED(401, "N-001", "Unauthorized"),
    NOT_FOUND(404, "N-004", "Not found"),
    METHOD_NOT_ALLOWED(404, "N-014", "Method Not Allowed"),
    UNSUPPORTED_MEDIA_TYPE(409, "N-009", "Unsupported Media Type"),
    INTERNAL_SERVER_ERROR(500, "N-500", "Internal Server Error"),
}