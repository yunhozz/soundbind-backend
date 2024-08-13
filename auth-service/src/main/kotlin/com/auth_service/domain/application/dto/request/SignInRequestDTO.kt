package com.auth_service.domain.application.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class SignInRequestDTO(
    @field:Email(message = "이메일을 입력해주세요")
    val email: String,
    @field:NotBlank(message = "비밀번호를 입력해주세요")
    val password: String
)
