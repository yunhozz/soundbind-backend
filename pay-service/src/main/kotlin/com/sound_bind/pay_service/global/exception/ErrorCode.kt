package com.sound_bind.pay_service.global.exception

enum class ErrorCode(
    val status: Int,
    val code: String,
    val message: String
) {
    BAD_REQUEST(400, "P-000", "Bad Request"),
    UNAUTHORIZED(401, "P-001", "Unauthorized"),
    NOT_FOUND(404, "P-004", "Not found"),
    METHOD_NOT_ALLOWED(404, "P-014", "Method Not Allowed"),
    UNSUPPORTED_MEDIA_TYPE(409, "P-009", "Unsupported Media Type"),
    INTERNAL_SERVER_ERROR(500, "P-500", "Internal Server Error"),
}