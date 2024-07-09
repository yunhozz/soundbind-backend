package com.sound_bind.review_service.domain.persistence.repository

import com.sound_bind.review_service.domain.persistence.entity.Comment
import com.sound_bind.review_service.domain.persistence.entity.Review
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository: JpaRepository<Comment, Long> {

    fun findCommentsByReview(review: Review): List<Comment>
}