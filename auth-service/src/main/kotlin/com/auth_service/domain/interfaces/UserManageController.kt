package com.auth_service.domain.interfaces

import com.auth_service.domain.application.UserManageService
import com.auth_service.domain.interfaces.dto.APIResponse
import com.auth_service.global.dto.request.SignUpRequestDTO
import jakarta.validation.Valid
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
        return APIResponse.of("Local User Joined", result)
        return APIResponse.of("Local user joined success", result)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun withdrawMember(@PathVariable id: String): APIResponse {
        userManageService.deleteLocalUser(id.toLong())
        return APIResponse.of("Withdraw successful")
    }
}