package com.sound_bind.global.dto

data class ApiResponse<T> private constructor(
    val message: String,
    val data: T?
) {
    companion object {
        fun <T> of(message: String, data: T? = null) = ApiResponse(message, data)
    }
}