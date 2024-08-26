package com.sound_bind.api_gateway.handler.exception

enum class ErrorCode(
    val status: Int,
    val code: String,
    val message: String
) {
    FRAME_WORK_INTERNAL_ERROR(500, "C-001", "Internal framework exceptions"),
    UNDEFINED_ERROR(500, "C-002", "Undefined exception"),
}