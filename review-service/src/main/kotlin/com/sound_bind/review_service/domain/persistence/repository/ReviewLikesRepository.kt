package com.sound_bind.review_service.domain.persistence.repository

import com.sound_bind.review_service.domain.persistence.entity.Review
import com.sound_bind.review_service.domain.persistence.entity.ReviewLikes
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ReviewLikesRepository: JpaRepository<ReviewLikes, Long> {

    fun findByReview(review: Review): List<ReviewLikes>

    @Query("select rl from ReviewLikes rl join fetch rl.review r where r.id = :reviewId and rl.userId = :userId")
    fun findWithReviewByReviewIdAndUserId(reviewId: Long, userId: Long): ReviewLikes?

    fun findReviewLikesByFlag(flag: Boolean): List<ReviewLikes>
}