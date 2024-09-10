package com.auth_service.domain.application.listener

import com.auth_service.domain.application.MailService
import org.springframework.stereotype.Component

@Component
class AsyncListenerImpl(private val mailService: MailService): AsyncListener {

    override fun onSendVerifyingEmail(email: String) =
        mailService.sendVerifyingEmail(email)
}