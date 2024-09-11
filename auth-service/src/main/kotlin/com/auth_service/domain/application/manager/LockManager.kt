package com.auth_service.domain.application.manager

interface LockManager {
    fun checkEmailDuplicatedWithLock(email: String): Boolean
}