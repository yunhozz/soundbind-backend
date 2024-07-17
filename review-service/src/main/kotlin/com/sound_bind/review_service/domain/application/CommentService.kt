package com.sound_bind.review_service.domain.application

import com.sound_bind.review_service.domain.interfaces.handler.CommentServiceException.CommentUpdateNotAuthorizedException
import com.sound_bind.review_service.domain.interfaces.handler.ReviewServiceException
import com.sound_bind.review_service.domain.persistence.entity.Comment
import com.sound_bind.review_service.domain.persistence.repository.CommentRepository
import com.sound_bind.review_service.domain.persistence.repository.ReviewRepository
import com.sound_bind.review_service.global.dto.request.CommentCreateDTO
import com.sound_bind.review_service.global.dto.response.CommentQueryDTO
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val reviewRepository: ReviewRepository
) {

    @Transactional
    fun createComment(reviewId: Long, userId: Long, dto: CommentCreateDTO): Long? {
        val review = reviewRepository.findById(reviewId)
            .orElseThrow { ReviewServiceException.ReviewNotFoundException("Review not found: $reviewId") }
        val comment = Comment.create(
            review,
            userId,
            dto.userNickname,
            dto.message
        )
        review.addComments(1)
        commentRepository.save(comment)
        return comment.id
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
}