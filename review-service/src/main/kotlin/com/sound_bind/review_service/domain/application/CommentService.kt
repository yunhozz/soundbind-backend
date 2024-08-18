package com.sound_bind.review_service.domain.application

import com.sound_bind.review_service.domain.application.dto.response.CommentIdReviewerIdDTO
import com.sound_bind.review_service.domain.persistence.entity.Comment
import com.sound_bind.review_service.domain.persistence.repository.CommentRepository
import com.sound_bind.review_service.domain.persistence.repository.ReviewRepository
import com.sound_bind.review_service.domain.persistence.repository.dto.CommentQueryDTO
import com.sound_bind.review_service.global.exception.CommentServiceException.CommentUpdateNotAuthorizedException
import com.sound_bind.review_service.global.exception.ReviewServiceException.ReviewNotFoundException
import com.sound_bind.review_service.global.util.RedisUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val reviewRepository: ReviewRepository
) {

    @Transactional
    fun createComment(reviewId: Long, userId: Long, message: String): CommentIdReviewerIdDTO {
        val review = reviewRepository.findById(reviewId)
            .orElseThrow { ReviewNotFoundException("Review not found: $reviewId") }
        val userInfo = RedisUtils.getJson("user:$userId", Map::class.java)
            ?: throw IllegalArgumentException("Value is not Present by Key : user:$userId")
        val comment = Comment.create(
            review,
            userId,
            userInfo["nickname"] as String,
            message
        )

        review.addComments(1)
        commentRepository.save(comment)
        return CommentIdReviewerIdDTO(comment.id!!, review.userId)
    }

    @Transactional(readOnly = true)
    fun findCommentListByReviewId(reviewId: Long): List<CommentQueryDTO> =
        commentRepository.findCommentsByReviewId(reviewId)

    @Transactional
    fun deleteComment(commentId: Long, userId: Long) {
        val comment = (commentRepository.findWithReviewByIdAndUserId(commentId, userId)
            ?: throw CommentUpdateNotAuthorizedException("Not Authorized for Delete"))
        comment.softDelete() // subtract review's comment number
    }

    fun getUserInformationOnRedis(userId: Long) =
        RedisUtils.getJson("user:$userId", Map::class.java)
            ?: throw IllegalArgumentException("Value is not Present by Key : user:$userId")
}