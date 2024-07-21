package com.auth_service.domain.interfaces

import com.auth_service.domain.application.AuthService
import com.auth_service.domain.interfaces.dto.APIResponse
import com.auth_service.global.dto.request.SignInRequestDTO
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun loginByLocalUser(@Valid @RequestBody dto: SignInRequestDTO): APIResponse {
        val result = authService.signInByLocalUser(dto)
        return APIResponse.of("Login successful", result)
    }
}