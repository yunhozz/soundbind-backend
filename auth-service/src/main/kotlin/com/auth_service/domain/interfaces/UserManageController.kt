package com.auth_service.domain.interfaces

import com.auth_service.domain.application.UserManageService
import com.auth_service.domain.interfaces.dto.APIResponse
import com.auth_service.global.dto.request.SignUpRequestDTO
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.validation.Valid
import khttp.post
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun withdrawMember(@PathVariable id: String): APIResponse {
        userManageService.deleteLocalUser(id.toLong())
        val record = mapOf(
            "topic" to "user-deletion-topic",
            "message" to mapOf("userId" to id)
        )
        post(
            url = "http://localhost:8000/api/kafka",
            headers = mapOf("Content-Type" to "application/json"),
            data = jacksonObjectMapper().writeValueAsString(record)
        )
        return APIResponse.of("Withdraw successful")
    }
}