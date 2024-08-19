package com.music_service.global.exception

enum class ErrorCode(
    val status: Int,
    val code: String,
    val message: String
) {
    BAD_REQUEST(400, "M-000", "Bad Request"),
    UNAUTHORIZED(401, "M-001", "Unauthorized"),
    NOT_FOUND(404, "M-004", "Not found"),
    METHOD_NOT_ALLOWED(404, "M-014", "Method Not Allowed"),
    UNSUPPORTED_MEDIA_TYPE(409, "M-009", "Unsupported Media Type"),
    INTERNAL_SERVER_ERROR(500, "M-500", "Internal Server Error"),
}