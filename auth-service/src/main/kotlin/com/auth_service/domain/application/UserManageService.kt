package com.auth_service.domain.application

import com.auth_service.domain.application.dto.request.SignUpRequestDTO
import com.auth_service.domain.persistence.entity.User
import com.auth_service.domain.persistence.entity.UserPassword
import com.auth_service.domain.persistence.entity.UserProfile
import com.auth_service.domain.persistence.repository.UserPasswordRepository
import com.auth_service.domain.persistence.repository.UserProfileRepository
import com.auth_service.domain.persistence.repository.UserRepository
import com.auth_service.global.exception.UserManageException.EmailDuplicateException
import com.auth_service.global.exception.UserManageException.UserNotFoundException
import com.auth_service.global.util.RedisUtils
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class UserManageService(
    private val userRepository: UserRepository,
    private val userPasswordRepository: UserPasswordRepository,
    private val userProfileRepository: UserProfileRepository,
    private val encoder: BCryptPasswordEncoder
) {

    @Transactional
    fun createLocalUser(dto: SignUpRequestDTO): Long {
        if (userProfileRepository.existsByEmail(dto.email)) {
            throw EmailDuplicateException("User email already exists")
        }
        val guest = User.createGuest()
        val password = UserPassword.create(guest, encoder.encode(dto.password))
        val profile = UserProfile.create(
            guest,
            dto.email,
            dto.name,
            dto.nickname,
            dto.profileUrl
        )

        userRepository.save(guest)
        userPasswordRepository.save(password)
        userProfileRepository.save(profile)

        return guest.id!!
    }

    @Transactional(readOnly = true)
    fun saveUserSimpleInfoOnRedis(email: String) {
        val userInfo = userProfileRepository.findSimpleInfoByEmail(email)
            ?: throw UserNotFoundException("User not found with email: $email")
        val userInfoStr = jacksonObjectMapper().writeValueAsString(userInfo)
        RedisUtils.saveValue("user:${userInfo.getId()}", userInfoStr, Duration.ofDays(1))
    }

    @Transactional
    fun deleteLocalUser(userId: Long, token: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException("User not found : $userId") }
        RedisUtils.deleteValue(token)
        RedisUtils.deleteValue("user:${user.id}")

        userPasswordRepository.deleteByUser(user)
        userProfileRepository.deleteByUser(user)
        userRepository.delete(user)
    }
}