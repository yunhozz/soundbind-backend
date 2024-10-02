package com.sound_bind.pay_service.domain.interfaces.dto

data class ApiResponse<T> private constructor(
    val message: String,
    val data: T?
) {
    companion object {
        fun <T> of(message: String, data: T? = null) = ApiResponse(message, data)
    }
}