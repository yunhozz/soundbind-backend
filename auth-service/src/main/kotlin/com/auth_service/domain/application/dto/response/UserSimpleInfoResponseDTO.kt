package com.auth_service.domain.application.dto.response

data class UserSimpleInfoResponseDTO(
    val userId: Long,
    val nickname: String,
    val profileUrl: String?
)
