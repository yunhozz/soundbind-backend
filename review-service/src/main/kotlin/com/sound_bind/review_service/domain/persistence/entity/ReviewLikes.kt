package com.sound_bind.review_service.domain.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne

@Entity
class ReviewLikes(
    val userId: Long,
    @ManyToOne(fetch = FetchType.LAZY)
    val review: Review,
    flag: Boolean = true
): BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    var flag = flag
        protected set

    fun changeFlag() {
        flag = flag.not()
        review.apply { if (flag) addLikes(1) else subtractLikes(1) }
    }
}