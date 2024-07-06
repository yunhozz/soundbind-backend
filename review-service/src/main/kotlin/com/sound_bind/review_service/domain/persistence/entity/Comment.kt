package com.sound_bind.review_service.domain.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

@Entity
@SQLRestriction("deleted_at is null")
class Comment private constructor(
    val userId: Long,
    @ManyToOne(fetch = FetchType.LAZY)
    val review: Review,
    userNickname: String,
    val message: String
): BaseEntity() {

    companion object {
        fun create(
            userId: Long,
            review: Review,
            userNickname: String,
            message: String
        ) = Comment(userId, review, userNickname, message)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    var userNickname = userNickname
        protected set

    var deletedAt: LocalDateTime? = null

    fun softDelete() = deletedAt ?: LocalDateTime.now()
}

/**
 * 리뷰와 달리 업데이트 불가능, 오직 생성과 삭제만 존재
 */