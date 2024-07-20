package com.auth_service.domain.application

import com.auth_service.domain.persistence.repository.UserPasswordRepository
import com.auth_service.domain.persistence.repository.UserRepository
import com.auth_service.global.auth.UserDetailsImpl
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserDetailsServiceImpl(
    private val userRepository: UserRepository,
    private val userPasswordRepository: UserPasswordRepository
): UserDetailsService {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String): UserDetails {
        userRepository.findById(username.toLong())
            .orElseThrow { UsernameNotFoundException("User ID $username not found") }
            .also { user ->
                val userPassword = userPasswordRepository.findByUser(user)
                    ?: throw UsernameNotFoundException("User Password $user does not exist")
                return UserDetailsImpl(user.id!!, userPassword.password, user.roles)
            }
    }
}