package com.sound_bind.api_gateway.handler.exception

import org.springframework.validation.BindingResult

data class ErrorResponse private constructor(
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
}