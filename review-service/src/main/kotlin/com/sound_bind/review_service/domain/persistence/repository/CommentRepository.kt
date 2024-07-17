package com.sound_bind.review_service.domain.persistence.repository

import com.sound_bind.review_service.domain.persistence.entity.Comment
import com.sound_bind.review_service.domain.persistence.entity.Review
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CommentRepository: JpaRepository<Comment, Long>, CommentQueryRepository {

    fun findCommentsByReview(review: Review): List<Comment>

    @Query("select c from Comment c join fetch c.review r where c.id = :commentId and c.userId = :userId")
    fun findWithReviewByIdAndUserId(commentId: Long, userId: Long): Comment?
}