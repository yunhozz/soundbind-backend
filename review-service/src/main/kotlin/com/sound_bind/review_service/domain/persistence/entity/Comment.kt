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
    @ManyToOne(fetch = FetchType.LAZY)
    val review: Review,
    val userId: Long,
    userNickname: String,
    val message: String
): BaseEntity() {

    companion object {
        fun create(
            review: Review,
            userId: Long,
            userNickname: String,
            message: String
        ) = Comment(review, userId, userNickname, message)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    var userNickname = userNickname
        protected set

    private var deletedAt: LocalDateTime? = null

    fun softDelete() = deletedAt ?: LocalDateTime.now()
}

/**
 * 리뷰와 달리 업데이트 불가능, 오직 생성과 삭제만 존재
 */