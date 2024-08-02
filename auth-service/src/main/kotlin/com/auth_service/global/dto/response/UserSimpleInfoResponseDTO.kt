package com.auth_service.global.dto.response

data class UserSimpleInfoResponseDTO(
    val userId: Long,
    val nickname: String,
    val profileUrl: String?
)
