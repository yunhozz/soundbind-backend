package com.sound_bind.review_service.global.exception

enum class ErrorCode(
    val status: Int,
    val code: String,
    val message: String
) {
    BAD_REQUEST(400, "R-000", "Bad Request"),
    UNAUTHORIZED(401, "R-001", "Unauthorized"),
    NOT_FOUND(404, "R-004", "Not found"),
    METHOD_NOT_ALLOWED(404, "R-014", "Method Not Allowed"),
    UNSUPPORTED_MEDIA_TYPE(409, "R-009", "Unsupported Media Type"),
    INTERNAL_SERVER_ERROR(500, "R-500", "Internal Server Error"),
}