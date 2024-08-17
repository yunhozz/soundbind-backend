package com.sound_bind.notification_service.domain.persistence.repository

import com.sound_bind.notification_service.domain.persistence.entity.Notification
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository

@Repository
class NotificationQueryRepositoryImpl(private val template: MongoTemplate): NotificationQueryRepository {

    override fun findAndCheckNotificationsInPage(userId: String, pageable: Pageable) {
        val query = Query().apply {
            addCriteria(Criteria.where("userId").`is`(userId))
            skip(pageable.offset)
            limit(pageable.pageSize)
            with(Sort.by(Sort.Direction.DESC, "createdAt"))
        }
        template.find(query, Notification::class.java).forEach { notification ->
            val updateQuery = Query(Criteria.where("_id").`is`(notification.id))
            val update = Update().apply { set("isChecked", true) }
            template.updateFirst(updateQuery, update, Notification::class.java)
        }
    }
}