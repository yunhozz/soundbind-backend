package com.auth_service.domain.interfaces

import com.auth_service.domain.application.UserManageService
import com.auth_service.domain.application.dto.request.SignUpRequestDTO
import com.auth_service.domain.interfaces.dto.APIResponse
import com.auth_service.global.annotation.HeaderSubject
import com.auth_service.global.annotation.HeaderToken
import com.auth_service.global.util.CookieUtils
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import khttp.post
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserManageController(private val userManageService: UserManageService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "유저 회원가입")
    fun signUpByLocalUser(@Valid @RequestBody dto: SignUpRequestDTO): APIResponse {
        val result = userManageService.createLocalUser(dto)
        return APIResponse.of("Local user joined success", result)
    }

    @PostMapping("/verify/email")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "회원가입 이메일 인증")
    fun verifyByEmail(@HeaderSubject sub: String, @RequestParam(required = true) code: String): APIResponse {
        userManageService.verifyByEmail(sub.toLong(), code)
        return APIResponse.of("Email verification success")
    }

    @PostMapping("/verify/email/resend")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "인증 이메일 재전송 요청")
    fun resendVerifyingEmail(@HeaderSubject sub: String): APIResponse {
        userManageService.resendVerifyingEmailByUserId(sub.toLong())
        return APIResponse.of("Verifying email is sent now")
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "유저 회원 탈퇴")
    fun withdrawMember(
        @HeaderToken token: String,
        @HeaderSubject sub: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): APIResponse {
        userManageService.deleteLocalUser(sub.toLong(), token)
        CookieUtils.deleteAllCookies(request, response)
        val record = mapOf(
            "topic" to "user-deletion-topic",
            "message" to mapOf("userId" to sub.toLong())
        )
        post(
            url = kafkaRequestUri,
            headers = mapOf("Content-Type" to "application/json"),
            data = jacksonObjectMapper().writeValueAsString(record)
        )
        return APIResponse.of("Withdraw success")
    }

    @Value("\${uris.kafka-server-uri:http://localhost:9000}/api/kafka")
    private lateinit var kafkaRequestUri: String
}