package com.auth_service.domain.application.listener

interface AsyncListener {
    fun onSendVerifyingEmail(email: String)
}