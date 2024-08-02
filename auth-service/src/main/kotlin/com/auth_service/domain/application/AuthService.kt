package com.auth_service.domain.application

import com.auth_service.domain.persistence.repository.UserPasswordRepository
import com.auth_service.domain.persistence.repository.UserProfileRepository
import com.auth_service.global.auth.jwt.JwtProvider
import com.auth_service.global.dto.request.SignInRequestDTO
import com.auth_service.global.dto.response.SubjectResponseDTO
import com.auth_service.global.dto.response.TokenResponseDTO
import com.auth_service.global.exception.AuthException.PasswordInvalidException
import com.auth_service.global.exception.AuthException.PasswordNotFoundException
import com.auth_service.global.exception.AuthException.TokenNotFoundException
import com.auth_service.global.exception.AuthException.UserNotFoundException
import com.auth_service.global.util.RedisUtils
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class AuthService(
    private val userPasswordRepository: UserPasswordRepository,
    private val userProfileRepository: UserProfileRepository,
    private val jwtProvider: JwtProvider,
    private val passwordEncoder: BCryptPasswordEncoder
) {

    @Transactional(readOnly = true)
    fun signInByLocalUser(dto: SignInRequestDTO): TokenResponseDTO {
        val userProfile = userProfileRepository.findWithUserByEmail(dto.email)
            ?: throw UserNotFoundException("User not found")

        val user = userProfile.user
        val userPassword = userPasswordRepository.findWithUserByUserId(user.id!!)
            ?: throw PasswordNotFoundException("Password not found")

        if (passwordEncoder.matches(dto.password, userPassword.password)) {
            val found = userPassword.user
            val tokenResponseDTO = jwtProvider.generateToken(found.id.toString(), found.role)
            saveRefreshTokenOnRedis(tokenResponseDTO)
            return tokenResponseDTO

        } else {
            throw PasswordInvalidException("Invalid password")
        }
    }

    fun signOut(token: String): Authentication =
        RedisUtils.getValue(token)?.let { refreshToken ->
            val authentication = jwtProvider.getAuthentication(token)
            RedisUtils.deleteValue(token)
            RedisUtils.saveValue(refreshToken, "LOGOUT", Duration.ofMinutes(10))

            authentication

        } ?: throw TokenNotFoundException("Token not found")

    fun tokenRefresh(token: String): TokenResponseDTO =
        RedisUtils.getValue(token)?.let { refreshToken ->
            val authentication = jwtProvider.getAuthentication(refreshToken)
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
}