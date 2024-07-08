package com.sound_bind.review_service.domain.persistence.repository

import com.sound_bind.review_service.domain.persistence.entity.Review
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ReviewRepository: JpaRepository<Review, Long> {

    fun existsReviewByMusicIdAndUserId(reviewId: Long, userId: Long): Boolean

    @Query(
        "select case " +
        "when ((r.createdAt < CURRENT_DATE - 30) or (r.updatedAt < CURRENT_DATE - 30)) then true " +
        "else false end " +
        "from Review r " +
        "where r.id = :reviewId"
    )
    fun isReviewEligibleForUpdate(reviewId: Long): Boolean
}