package com.auth_service.global.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class SignUpRequestDTO(
    @field:Email(message = "이메일 형식에 맞지 않습니다")
    val email: String,
    @field:NotBlank(message = "패스워드를 입력해주세요")
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{7,}$",
        message = "패스워드는 최소 7자리 이상이어야 하며, 대소문자를 포함해야 합니다."
    )
    val password: String,
    @field:NotBlank(message = "성함을 입력해주세요")
    val name: String,
    @field:Size(min = 3, max = 10, message = "길이가 최소 3, 최대 10이어야 합니다")
    val nickname: String,
    val profileUrl: String?
)
