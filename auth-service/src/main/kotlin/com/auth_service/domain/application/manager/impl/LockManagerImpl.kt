package com.auth_service.domain.application.manager.impl

import com.auth_service.domain.application.manager.LockManager
import com.auth_service.domain.persistence.repository.UserProfileRepository
import com.auth_service.domain.persistence.repository.dto.UserSimpleInfoQueryDTO
import com.auth_service.global.annotation.DistributedLock
import org.springframework.stereotype.Component

@Component
class LockManagerImpl(private val userProfileRepository: UserProfileRepository): LockManager {

    @DistributedLock(
        key = "'check-email-nickname-duplicated-lock' + #email + '-' + #nickname",
        leaseTime = 5
    )
    override fun checkEmailAndNicknameDuplicatedWithLock(email: String, nickname: String): Boolean =
        userProfileRepository.existsByEmail(email) || userProfileRepository.existsByNickname(nickname)

    @DistributedLock(
        key = "'find-simple-info-lock' + #email",
        leaseTime = 5
    )
    override fun findSimpleInfoByEmailWithLock(email: String): UserSimpleInfoQueryDTO? =
        userProfileRepository.findSimpleInfoByEmail(email)
}