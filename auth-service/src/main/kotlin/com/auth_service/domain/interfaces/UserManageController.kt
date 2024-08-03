package com.auth_service.domain.interfaces

import com.auth_service.domain.application.UserManageService
import com.auth_service.domain.application.dto.request.SignUpRequestDTO
import com.auth_service.domain.interfaces.dto.APIResponse
import com.auth_service.global.annotation.HeaderSubject
import com.auth_service.global.annotation.HeaderToken
import com.auth_service.global.util.CookieUtils
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import khttp.post
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserManageController(private val userManageService: UserManageService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun signUpByLocalUser(@Valid @RequestBody dto: SignUpRequestDTO): APIResponse {
        val result = userManageService.createLocalUser(dto)
        return APIResponse.of("Local user joined success", result)
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun withdrawMember(
        @HeaderToken token: String,
        @HeaderSubject id: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): APIResponse {
        userManageService.deleteLocalUser(id.toLong(), token)
        CookieUtils.deleteAllCookies(request, response)
        val record = mapOf(
            "topic" to "user-deletion-topic",
            "message" to mapOf("userId" to id)
        )
        post(
            url = "http://localhost:9000/api/kafka",
            headers = mapOf("Content-Type" to "application/json"),
            data = jacksonObjectMapper().writeValueAsString(record)
        )
        return APIResponse.of("Withdraw successful")
    }
}