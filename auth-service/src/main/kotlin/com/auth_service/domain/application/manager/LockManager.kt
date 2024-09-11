package com.auth_service.domain.application.manager

import com.auth_service.domain.persistence.repository.dto.UserSimpleInfoQueryDTO

interface LockManager {
    fun checkEmailAndNicknameDuplicatedWithLock(email: String, nickname: String): Boolean
    fun findSimpleInfoByEmailWithLock(email: String): UserSimpleInfoQueryDTO?
}