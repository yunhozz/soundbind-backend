package com.auth_service.domain.application

import com.auth_service.domain.interfaces.handler.UserManageException.EmailDuplicateException
import com.auth_service.domain.persistence.entity.User
import com.auth_service.domain.persistence.entity.UserPassword
import com.auth_service.domain.persistence.entity.UserProfile
import com.auth_service.domain.persistence.repository.UserPasswordRepository
import com.auth_service.domain.persistence.repository.UserProfileRepository
import com.auth_service.domain.persistence.repository.UserRepository
import com.auth_service.global.dto.request.SignUpRequestDTO
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
}