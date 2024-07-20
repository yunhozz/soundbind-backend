package com.auth_service.global.dto.response

data class TokenResponseDTO(
    val accessToken: String,
    val refreshToken: String
)
