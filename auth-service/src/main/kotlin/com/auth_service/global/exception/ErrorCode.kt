package com.auth_service.global.exception

enum class ErrorCode(
    val status: Int,
    val code: String,
    val message: String
) {
    BAD_REQUEST(400, "C-001", "Bad Request"),
    UNAUTHORIZED(401, "C-002", "Unauthorized"),
    FORBIDDEN(403, "C-003", "Forbidden"),
    NOT_FOUND(404, "C-004", "Not Found"),
    METHOD_NOT_ALLOWED(405, "C-005", "Method Not Allowed"),
    FRAME_WORK_INTERNAL_ERROR(500, "C-000", "Internal Framework Exception"),
    UNDEFINED_ERROR(500, "C-999", "Undefined Exception")
}