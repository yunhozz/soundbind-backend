package com.sound_bind.review_service.domain.persistence.repository

import com.sound_bind.review_service.domain.persistence.entity.Review
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface ReviewRepository: JpaRepository<Review, Long>, ReviewQueryRepository {

    fun existsReviewByMusicIdAndUserId(musicId: Long, userId: Long): Boolean

    fun findReviewByIdAndUserId(reviewId: Long, userId: Long): Review?

    fun findReviewsByUserId(userId: Long): List<Review>

    @Modifying(clearAutomatically = true)
    @Query("update Review r set r.deletedAt = :now where r.userId = :userId")
    fun deleteReviewsByUserId(now: LocalDateTime, userId: Long)

    @Query(
        "select case " +
        "when (r.createdAt < :cutoffDate and r.updatedAt < :cutoffDate) then true " +
        "else false end " +
        "from Review r " +
        "where r.id = :reviewId"
    )
    fun isReviewEligibleForUpdate(reviewId: Long, cutoffDate: LocalDateTime): Boolean
}