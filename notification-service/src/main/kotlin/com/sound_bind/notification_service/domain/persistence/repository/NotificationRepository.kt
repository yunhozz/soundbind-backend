package com.sound_bind.notification_service.domain.persistence.repository

import com.sound_bind.notification_service.domain.persistence.entity.Notification
import org.springframework.data.mongodb.repository.MongoRepository

interface NotificationRepository: MongoRepository<Notification, Long>