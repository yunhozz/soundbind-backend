package com.auth_service.domain.application

import com.auth_service.domain.application.dto.request.SignInRequestDTO
import com.auth_service.domain.application.dto.response.SubjectResponseDTO
import com.auth_service.domain.application.manager.LockManager
import com.auth_service.domain.persistence.repository.UserPasswordRepository
import com.auth_service.domain.persistence.repository.dto.UserSimpleInfoQueryDTO
import com.auth_service.global.auth.jwt.JwtProvider
import com.auth_service.global.auth.jwt.TokenResponseDTO
import com.auth_service.global.exception.AuthException.PasswordInvalidException
import com.auth_service.global.exception.AuthException.PasswordNotFoundException
import com.auth_service.global.exception.AuthException.TokenNotFoundException
import com.auth_service.global.exception.UserManageException
import com.auth_service.global.util.RedisUtils
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class AuthService(
    private val userPasswordRepository: UserPasswordRepository,
    private val lockManager: LockManager,
    private val jwtProvider: JwtProvider,
    private val passwordEncoder: BCryptPasswordEncoder
) {

    @Transactional(readOnly = true)
    fun signInByLocalUser(dto: SignInRequestDTO): TokenResponseDTO {
        val userProfile = lockManager.findSimpleInfoByEmailWithLock(dto.email)
            ?: throw UserManageException.UserNotFoundException("User not found with email: ${dto.email}")
        val userPassword = userPasswordRepository.findWithUserByUserId(userProfile.getUserId())
            ?: throw PasswordNotFoundException("Password not found")

        if (passwordEncoder.matches(dto.password, userPassword.password)) {
            saveUserSimpleInformationOnRedis(userProfile)
            val found = userPassword.user
            val tokenResponseDTO = jwtProvider.generateToken(found.id.toString(), found.role)
            saveRefreshTokenOnRedis(tokenResponseDTO)

            return tokenResponseDTO

        } else {
            throw PasswordInvalidException("Password incorrect")
        }
    }

    fun signOut(token: String): Authentication =
        RedisUtils.getValue(token)?.let {
            val authentication = jwtProvider.getAuthentication(it)
            RedisUtils.updateValue(token, "LOGOUT", Duration.ofMinutes(10))
            authentication

        } ?: throw TokenNotFoundException("Token not found")

    fun tokenRefresh(token: String): TokenResponseDTO =
        RedisUtils.getValue(token)?.let {
            val authentication = jwtProvider.getAuthentication(it)
            val tokenResponseDTO = jwtProvider.generateToken(authentication)

            RedisUtils.deleteValue(token)
            saveRefreshTokenOnRedis(tokenResponseDTO)
            tokenResponseDTO

        } ?: throw TokenNotFoundException("Token not found. Need login.")

    fun getSubjectByToken(token: String): SubjectResponseDTO {
        val authentication = jwtProvider.getAuthentication(token)
        val authorities = authentication.authorities
        return SubjectResponseDTO(authentication.name, authorities.first().authority)
    }

    private fun saveRefreshTokenOnRedis(tokenResponseDTO: TokenResponseDTO) =
        RedisUtils.saveValue(
            tokenResponseDTO.accessToken,
            tokenResponseDTO.refreshToken,
            Duration.ofMillis(tokenResponseDTO.refreshTokenValidTime)
        )

    private fun saveUserSimpleInformationOnRedis(userProfile: UserSimpleInfoQueryDTO) {
        val userInfoStr = jacksonObjectMapper().writeValueAsString(userProfile)
        RedisUtils.saveValue("user:${userProfile.getUserId()}", userInfoStr, Duration.ofDays(1))
    }
}