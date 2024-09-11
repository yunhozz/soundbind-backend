package com.auth_service.domain.application.manager.impl

import com.auth_service.domain.application.manager.LockManager
import com.auth_service.domain.persistence.repository.UserProfileRepository
import com.auth_service.global.annotation.DistributedSimpleLock
import org.springframework.stereotype.Component

@Component
class LockManagerImpl(private val userProfileRepository: UserProfileRepository): LockManager {

    @DistributedSimpleLock(key = "email-duplicate-check")
    override fun checkEmailDuplicatedWithLock(email: String): Boolean =
        userProfileRepository.existsByEmail(email)
}