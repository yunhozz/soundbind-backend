package com.sound_bind.notification_service.domain.persistence.repository

import org.springframework.data.domain.Pageable

interface NotificationQueryRepository {
    fun findAndCheckNotificationsInPage(userId: String, pageable: Pageable)
}