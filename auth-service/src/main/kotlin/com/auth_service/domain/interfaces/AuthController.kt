package com.auth_service.domain.interfaces

import com.auth_service.domain.application.AuthService
import com.auth_service.domain.application.dto.request.SignInRequestDTO
import com.auth_service.domain.application.dto.response.SubjectResponseDTO
import com.auth_service.global.annotation.HeaderToken
import com.auth_service.global.auth.jwt.TokenResponseDTO
import com.sound_bind.global.dto.ApiResponse
import com.sound_bind.global.utils.CookieUtils
import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "유저 로그인")
    fun loginByLocalUser(@Valid @RequestBody dto: SignInRequestDTO, response: HttpServletResponse): ApiResponse<TokenResponseDTO> {
        val result = authService.signInByLocalUser(dto)
        CookieUtils.addCookie(
            response,
            CookieUtils.ACCESS_TOKEN_COOKIE_NAME,
            CookieUtils.serialize(result.accessToken),
            null
        )
        return ApiResponse.of("Login successful", result)
    }

    @DeleteMapping("/logout")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "유저 로그아웃")
    fun signOut(
        @HeaderToken token: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ApiResponse<Unit> {
        val authentication = authService.signOut(token)
        SecurityContextLogoutHandler().logout(request, response, authentication)
        CookieUtils.deleteAllCookies(request, response)
        return ApiResponse.of("Logout successful")
    }

    @GetMapping("/token/refresh")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "JWT 토큰 재발급")
    fun refreshToken(@CookieValue("atk") tokenCookie: Cookie, response: HttpServletResponse): TokenResponseDTO {
        val result = authService.tokenRefresh(CookieUtils.deserialize(tokenCookie, String::class.java))
        CookieUtils.addCookie(
            response,
            CookieUtils.ACCESS_TOKEN_COOKIE_NAME,
            CookieUtils.serialize(result.accessToken),
            null
        )
        return result
    }

    @GetMapping("/subject")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "유저의 JWT subject 조회")
    fun getSubject(@HeaderToken token: String): SubjectResponseDTO = authService.getSubjectByToken(token)
}