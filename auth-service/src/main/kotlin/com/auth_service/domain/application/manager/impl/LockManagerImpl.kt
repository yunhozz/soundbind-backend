package com.auth_service.domain.application.manager.impl

import com.auth_service.domain.application.manager.LockManager
import com.auth_service.domain.persistence.repository.UserProfileRepository
import com.auth_service.domain.persistence.repository.dto.UserSimpleInfoQueryDTO
import com.auth_service.global.annotation.DistributedSimpleLock
import org.springframework.stereotype.Component

@Component
class LockManagerImpl(private val userProfileRepository: UserProfileRepository): LockManager {

    @DistributedSimpleLock(key = "checkEmailDuplicated")
    override fun checkEmailAndNicknameDuplicatedWithLock(email: String, nickname: String): Boolean =
        userProfileRepository.existsByEmail(email) || userProfileRepository.existsByNickname(nickname)

    @DistributedSimpleLock(key = "findSimpleInfoByEmail")
    override fun findSimpleInfoByEmailWithLock(email: String): UserSimpleInfoQueryDTO? =
        userProfileRepository.findSimpleInfoByEmail(email)
}