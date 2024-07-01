package com.music_service.global.dto.response

import org.springframework.validation.BindingResult

data class ErrorResponse(
    val status: Int,
    val code: String,
    val message: String,
    val fieldErrors: List<FieldErrorResponse>
) {
    private constructor(errorCode: ErrorCode): this(status = errorCode.status, code = errorCode.code, message = errorCode.message, fieldErrors = emptyList())
    private constructor(errorCode: ErrorCode, message: String): this(status = errorCode.status, code = errorCode.code, message, fieldErrors = emptyList())
    private constructor(errorCode: ErrorCode, fieldErrors: List<FieldErrorResponse>): this(status = errorCode.status, code = errorCode.code, message = errorCode.message, fieldErrors)

    companion object {
        fun of(errorCode: ErrorCode): ErrorResponse = ErrorResponse(errorCode)
        fun of(errorCode: ErrorCode, message: String): ErrorResponse = ErrorResponse(errorCode, message)
        fun of(errorCode: ErrorCode, result: BindingResult): ErrorResponse = ErrorResponse(errorCode, FieldErrorResponse.of(result))
    }

    data class FieldErrorResponse(
        val field: String,
        val value: String,
        val reason: String?
    ) {
        companion object {
            fun of(field: String, value: String, reason: String): List<FieldErrorResponse> = listOf(FieldErrorResponse(field, value, reason))

            fun of(result: BindingResult): List<FieldErrorResponse> = result.fieldErrors.stream().map {
                FieldErrorResponse(
                    field = it.field,
                    value = (it.rejectedValue ?: "").toString(),
                    reason = it.defaultMessage)
                }.toList()
        }
    }

    enum class ErrorCode(
        val status: Int,
        val code: String,
        val message: String
    ) {
        INVALID_REQUEST(400, "M-001", "Invalid request"),
        INVALID_VALUE_TYPES(400, "M-002", "Invalid value types"),
        RESOURCE_NOT_FOUND(404, "M-004", "Resource not found"),
        METHOD_NOT_ALLOWED(405, "M-005", "Invalid Method"),
        PAYLOAD_TOO_LARGE(413, "M-013", "File size exceeds maximum limit"),
        UNSUPPORTED_MEDIA_TYPE(415, "M-015", "Media type not supported"),
        INTERNAL_SERVER_ERROR(500, "M-500", "Server Error");
    }
}
