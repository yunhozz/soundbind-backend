package com.auth_service.domain.application.manager

interface AsyncManager {
    fun onSendVerifyingEmail(email: String)
}