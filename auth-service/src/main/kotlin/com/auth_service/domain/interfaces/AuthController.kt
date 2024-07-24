package com.auth_service.domain.interfaces

import com.auth_service.domain.application.AuthService
import com.auth_service.domain.interfaces.dto.APIResponse
import com.auth_service.global.dto.request.SignInRequestDTO
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.CREATED)
    fun loginByLocalUser(@Valid @RequestBody dto: SignInRequestDTO): APIResponse {
        val result = authService.signInByLocalUser(dto)
        return APIResponse.of("Login successful", result)
    }

    @GetMapping("/token/reissue")
    @ResponseStatus(HttpStatus.CREATED)
    fun refreshToken(@RequestParam username: String): APIResponse {
        val result = authService.tokenReissue(username)
        return APIResponse.of("Token refresh successful", result)
    }

    @GetMapping("/subject")
    @ResponseStatus(HttpStatus.OK)
    fun getSubject(@RequestHeader(HttpHeaders.AUTHORIZATION) token: String): Mono<String> {
        val result = authService.getSubjectByToken(token)
        return Mono.just(result)
    }
}