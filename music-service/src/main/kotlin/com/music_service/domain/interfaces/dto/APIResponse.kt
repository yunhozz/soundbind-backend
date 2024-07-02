package com.music_service.domain.interfaces.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class APIResponse private constructor(
    val message: String,
    val data: Any?
) {
    companion object {
        fun of(message: String): APIResponse = APIResponse(message, null)
        fun of(message: String, data: Any?): APIResponse = APIResponse(message, data)
    }
}
