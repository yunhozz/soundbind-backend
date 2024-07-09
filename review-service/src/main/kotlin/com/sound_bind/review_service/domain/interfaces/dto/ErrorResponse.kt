package com.review_service.domain.interfaces.dto

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
                    reason = it.defaultMessage
                )
            }.toList()
        }
    }

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
}