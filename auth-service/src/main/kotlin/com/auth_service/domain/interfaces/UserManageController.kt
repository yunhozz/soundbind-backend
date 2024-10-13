package com.auth_service.domain.interfaces

import com.auth_service.domain.application.UserManageService
import com.auth_service.domain.application.dto.request.SignUpRequestDTO
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sound_bind.global.annotation.HeaderSubject
import com.sound_bind.global.annotation.HeaderToken
import com.sound_bind.global.dto.ApiResponse
import com.sound_bind.global.dto.KafkaEvent
import com.sound_bind.global.dto.KafkaMessage
import com.sound_bind.global.utils.CookieUtils
import com.sound_bind.global.utils.KafkaConstants
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

    private val mapper = jacksonObjectMapper()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "유저 회원가입")
    fun signUpByLocalUser(@Valid @RequestBody dto: SignUpRequestDTO): ApiResponse<Long> {
        val userId = userManageService.createLocalUser(dto)
        val userAddedEvent = KafkaEvent(
            topic = KafkaConstants.USER_ADDED_TOPIC,
            message = KafkaMessage.UserInfoMessage(userId)
        )
        sendEventsToKafkaProducer(userAddedEvent)

        return ApiResponse.of("Local user joined success", userId)
    }

    @PostMapping("/verify/email")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "회원가입 이메일 인증")
    fun verifyByEmail(@HeaderSubject sub: String, @RequestParam(required = true) code: String): ApiResponse<Unit> {
        userManageService.verifyByEmail(sub.toLong(), code)
        return ApiResponse.of("Email verification success")
    }

    @PostMapping("/verify/email/resend")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "인증 이메일 재전송 요청")
    fun resendVerifyingEmail(@HeaderSubject sub: String): ApiResponse<Unit> {
        userManageService.resendVerifyingEmailByUserId(sub.toLong())
        return ApiResponse.of("Verifying email is sent now")
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "유저 회원 탈퇴")
    fun withdrawMember(
        @HeaderToken token: String,
        @HeaderSubject sub: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ApiResponse<Unit> {
        userManageService.deleteLocalUser(sub.toLong(), token)

        CookieUtils.deleteAllCookies(request, response)

        val userDeletionEvent = KafkaEvent(
            topic = KafkaConstants.USER_DELETION_TOPIC,
            message = KafkaMessage.UserInfoMessage(sub.toLong())
        )
        sendEventsToKafkaProducer(userDeletionEvent)

        return ApiResponse.of("Withdraw success")
    }

    @Value("\${uris.kafka-server-uri:http://localhost:9000}/api/kafka")
    private lateinit var kafkaRequestUri: String

    private fun sendEventsToKafkaProducer(vararg events: KafkaEvent) =
        post(
            url = kafkaRequestUri,
            headers = mapOf("Content-Type" to "application/json"),
            data = mapper.writeValueAsString(events)
        )
}