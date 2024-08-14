package com.sound_bind.notification_service.domain.persistence.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "notification")
class Notification private constructor(
    val senderId: String,
    val receiverId: String,
    val message: String,
    isChecked: Boolean = false
) {

    companion object {
        fun create(senderId: String, receiverId: String, message: String) =
            Notification(senderId, receiverId, message)
    }

    @Id
    var id: String? = null

    var isChecked = isChecked
        protected set

    @CreatedDate
    var createdAt: LocalDateTime? = null

    @LastModifiedDate
    var updatedAt: LocalDateTime? = null

    fun check() {
        require(!isChecked) { "Notification is already checked" }
        isChecked = !isChecked
    }
}