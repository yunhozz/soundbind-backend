package com.auth_service.global.exception

enum class ErrorCode(
    val status: Int,
    val code: String,
    val message: String
) {
    BAD_REQUEST(400, "U-000", "Bad Request"),
    UNAUTHORIZED(401, "U-001", "Unauthorized"),
    NOT_FOUND(404, "U-004", "Not found"),
    METHOD_NOT_ALLOWED(404, "U-014", "Method Not Allowed"),
    UNSUPPORTED_MEDIA_TYPE(409, "U-009", "Unsupported Media Type"),
    INTERNAL_SERVER_ERROR(500, "U-500", "Internal Server Error"),
}