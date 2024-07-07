package com.review_service.domain.interfaces.dto

data class APIResponse private constructor(
    val message: String,
    val data: Any?
) {
    companion object {
        fun of(message: String): APIResponse = APIResponse(message, null)
        fun of(message: String, data: Any?): APIResponse = APIResponse(message, data)
    }
}
