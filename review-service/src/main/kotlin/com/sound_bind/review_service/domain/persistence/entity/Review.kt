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
    userImageUrl: String?,
    message: String,
    score: Double
): BaseEntity() {

    companion object {
        fun create(
            musicId: Long,
            userId: Long,
            userNickname: String,
            userImageUrl: String?,
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

    var commentNum = 0
        protected set

    var likes = 0
        protected set

    private var deletedAt: LocalDateTime? = null

    fun updateMessageAndScore(message: String, score: Double) {
        this.message = message
        this.score = score
    }

    fun addLikes(like: Int) {
        likes += like
    }

    fun subtractLikes(like: Int) {
        require(likes - like >= 0) { throw IllegalArgumentException("Likes must not be negative") }
        likes -= like
    }

    fun addComments(count: Int) {
        commentNum += count
    }

    fun subtractComments(count: Int) {
        require(commentNum - count >= 0) { throw IllegalArgumentException("Comments must not be negative") }
        commentNum -= count
    }

    fun softDelete() {
        deletedAt = deletedAt ?: LocalDateTime.now()
    }
}