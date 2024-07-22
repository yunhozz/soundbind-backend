package com.auth_service.domain.application

import com.auth_service.domain.interfaces.handler.AuthException.PasswordInvalidException
import com.auth_service.domain.interfaces.handler.AuthException.PasswordNotFoundException
import com.auth_service.domain.interfaces.handler.AuthException.UserNotFoundException
import com.auth_service.domain.persistence.repository.UserPasswordRepository
import com.auth_service.domain.persistence.repository.UserProfileRepository
import com.auth_service.global.auth.JwtProvider
import com.auth_service.global.dto.request.SignInRequestDTO
import com.auth_service.global.dto.response.TokenResponseDTO
import com.auth_service.global.util.RedisUtils
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class AuthService(
    private val userPasswordRepository: UserPasswordRepository,
    private val userProfileRepository: UserProfileRepository,
    private val jwtProvider: JwtProvider,
    private val encoder: BCryptPasswordEncoder
) {

    @Transactional(readOnly = true)
    fun signInByLocalUser(dto: SignInRequestDTO): TokenResponseDTO {
        val userProfile = userProfileRepository.findWithUserByEmail(dto.email)
            ?: throw UserNotFoundException("User not found")

        val user = userProfile.user
        val userPassword = userPasswordRepository.findWithUserByUserId(user.id!!)
            ?: throw PasswordNotFoundException("Password not found")

        if (encoder.matches(dto.password, userPassword.password)) {
            val found = userPassword.user
            val tokenResponseDTO = jwtProvider.generateToken(found.id.toString(), found.role)
            RedisUtils.saveValue(
                user.id.toString(),
                tokenResponseDTO.refreshToken,
                Duration.ofMillis(tokenResponseDTO.refreshTokenValidTime)
            )
            return tokenResponseDTO
        } else {
            throw PasswordInvalidException("Invalid password")
        }
    }
}