package com.auth_service.domain.interfaces

import com.auth_service.domain.application.AuthService
import com.auth_service.domain.interfaces.dto.APIResponse
import com.auth_service.global.dto.request.SignInRequestDTO
import com.auth_service.global.dto.response.TokenResponseDTO
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.CREATED)
    fun loginByLocalUser(@Valid @RequestBody dto: SignInRequestDTO): APIResponse {
        val result = authService.signInByLocalUser(dto)
        return APIResponse.of("Login successful", result)
    }

    @GetMapping("/token/refresh")
    @ResponseStatus(HttpStatus.OK)
    fun refreshToken(@RequestHeader("X-Token-Expired") token: String): TokenResponseDTO =
        authService.tokenRefresh(token)

    @GetMapping("/subject")
    @ResponseStatus(HttpStatus.OK)
    fun getSubject(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String): String =
        authService.getSubjectByToken(token)
}