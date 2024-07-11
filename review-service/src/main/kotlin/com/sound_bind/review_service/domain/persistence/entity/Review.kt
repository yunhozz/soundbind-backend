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
    val musicId: Long,
    val userId: Long,
    userNickname: String,
    userImageUrl: String,
    message: String,
    score: Double,
    comments: Int = 0,
    likes: Int = 0
): BaseEntity() {

    companion object {
        fun create(
            musicId: Long,
            userId: Long,
            userNickname: String,
            userImageUrl: String,
            message: String,
            score: Double
        ) = Review(musicId, userId, userNickname, userImageUrl, message, score)
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

    var comments = comments
        protected set

    var likes = likes
        protected set

    private var deletedAt: LocalDateTime? = null

    fun updateMessageAndScore(message: String, score: Double) {
        this.message = message
        this.score = score
    }

    fun addLikes(like: Int) = likes + like

    fun subtractLikes(like: Int): Int {
        val result = likes - like
        return result.takeIf { it >= 0 } ?: throw IllegalArgumentException("Likes must not be negative")
    }

    fun addComments(count: Int) = comments + count

    fun subtractComments(count: Int): Int {
        val result = comments - count
        return result.takeIf { it >= 0 } ?: throw IllegalArgumentException("Comments must not be negative")
    }

    fun softDelete(): LocalDateTime = deletedAt ?: LocalDateTime.now()
}