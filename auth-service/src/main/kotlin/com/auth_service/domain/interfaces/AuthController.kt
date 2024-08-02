package com.auth_service.domain.interfaces

import com.auth_service.domain.application.AuthService
import com.auth_service.domain.application.dto.request.SignInRequestDTO
import com.auth_service.domain.application.dto.response.SubjectResponseDTO
import com.auth_service.domain.interfaces.dto.APIResponse
import com.auth_service.global.annotation.HeaderToken
import com.auth_service.global.auth.jwt.TokenResponseDTO
import com.auth_service.global.util.CookieUtils
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
    fun loginByLocalUser(@Valid @RequestBody dto: SignInRequestDTO, response: HttpServletResponse): APIResponse {
        val result = authService.signInByLocalUser(dto)
        CookieUtils.addCookie(
            response,
            "atk",
            CookieUtils.serialize(result.accessToken),
            null
        )
        return APIResponse.of("Login successful", result)
    }

    @DeleteMapping("/logout")
    @ResponseStatus(HttpStatus.CREATED)
    fun signOut(
        @HeaderToken token: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): APIResponse {
        val authentication = authService.signOut(token)
        SecurityContextLogoutHandler().logout(request, response, authentication)
        CookieUtils.deleteCookie(request, response, "atk")
        return APIResponse.of("Logout successful")
    }

    @GetMapping("/token/refresh")
    @ResponseStatus(HttpStatus.OK)
    fun refreshToken(@CookieValue("atk") tokenCookie: Cookie, response: HttpServletResponse): TokenResponseDTO {
        val result = authService.tokenRefresh(CookieUtils.deserialize(tokenCookie, String::class.java))
        CookieUtils.addCookie(
            response,
            "atk",
            CookieUtils.serialize(result.accessToken),
            null
        )
        return result
    }

    @GetMapping("/subject")
    @ResponseStatus(HttpStatus.OK)
    fun getSubject(@HeaderToken token: String): SubjectResponseDTO = authService.getSubjectByToken(token)
}