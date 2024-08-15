package com.sound_bind.notification_service.domain.persistence.repository

import com.sound_bind.notification_service.domain.persistence.entity.Notification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface NotificationRepository: MongoRepository<Notification, String> {

    @Query(value = "{ receiverId: ?0 }", fields = "{ id: 1, message: 1, createdAt: 1 }")
    fun findSimpleNotificationsByReceiverId(receiverId: String, pageable: Pageable): Page<Notification>
}