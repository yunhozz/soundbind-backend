package com.auth_service.domain.application.manager.impl

import com.auth_service.domain.application.MailService
import com.auth_service.domain.application.manager.AsyncManager
import org.springframework.stereotype.Component

@Component
class AsyncManagerImpl(private val mailService: MailService): AsyncManager {

    override fun onSendVerifyingEmail(email: String) =
        mailService.sendVerifyingEmail(email)
}