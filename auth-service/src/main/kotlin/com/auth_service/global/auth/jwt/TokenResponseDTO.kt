package com.auth_service.global.auth.jwt

data class TokenResponseDTO(
    val tokenType: String,
    val accessToken: String,
    val refreshToken: String,
    val accessTokenValidTime: Long,
    val refreshTokenValidTime: Long
)
