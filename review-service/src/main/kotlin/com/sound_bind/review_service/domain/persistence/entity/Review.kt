package com.sound_bind.review_service.domain.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

@Entity
@SQLRestriction("deleted_at is null")
class Review private constructor(
    val userId: Long,
    val musicId: Long,
    userNickname: String,
    userImageUrl: String,
    message: String,
    score: Double,
    likes: Int = 0
): BaseEntity() {

    companion object {
        fun create(
            userId: Long,
            musicId: Long,
            userNickname: String,
            userImageUrl: String,
            message: String,
            score: Double
        ) = Review(userId, musicId, userNickname, userImageUrl, message, score)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    var userNickname = userNickname
        protected set

    var userImageUrl = userImageUrl
        protected set

    var message = message
        protected set

    var score = score
        protected set

    var likes = likes
        protected set

    var deletedAt: LocalDateTime? = null

    fun softDelete() = deletedAt ?: LocalDateTime.now()
}