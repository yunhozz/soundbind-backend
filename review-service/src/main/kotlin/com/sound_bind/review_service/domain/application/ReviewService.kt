package com.sound_bind.review_service.domain.application

import com.sound_bind.review_service.domain.interfaces.handler.ReviewServiceException.ReviewAlreadyExistException
import com.sound_bind.review_service.domain.interfaces.handler.ReviewServiceException.ReviewNotFoundException
import com.sound_bind.review_service.domain.interfaces.handler.ReviewServiceException.ReviewNotUpdatableException
import com.sound_bind.review_service.domain.interfaces.handler.ReviewServiceException.ReviewUpdateNotAuthorizedException
import com.sound_bind.review_service.domain.persistence.entity.Review
import com.sound_bind.review_service.domain.persistence.repository.CommentRepository
import com.sound_bind.review_service.domain.persistence.repository.ReviewQueryRepository.ReviewSort
import com.sound_bind.review_service.domain.persistence.repository.ReviewRepository
import com.sound_bind.review_service.global.dto.request.ReviewCreateDTO
import com.sound_bind.review_service.global.dto.request.ReviewCursorRequestDTO
import com.sound_bind.review_service.global.dto.request.ReviewUpdateDTO
import com.sound_bind.review_service.global.dto.response.ReviewQueryDTO
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val commentRepository: CommentRepository
) {

    @Transactional
    fun createReview(musicId: Long, userId: Long, dto: ReviewCreateDTO): Long? {
        if (reviewRepository.existsReviewByMusicIdAndUserId(musicId, userId)) {
            throw ReviewAlreadyExistException("Review already exists")
        }
        val review = Review.create(
            musicId,
            userId,
            dto.userNickname,
            dto.userImageUrl,
            dto.message,
            dto.score
        )
        reviewRepository.save(review)
        return review.id
    }

    @Transactional
    fun updateReviewMessageAndScore(reviewId: Long, userId: Long, dto: ReviewUpdateDTO): Long? {
        val review = reviewRepository.findReviewByIdAndUserId(reviewId, userId)
            ?: throw ReviewUpdateNotAuthorizedException("Not Authorized for Update")
        review.id?.let {
            val before30Days = LocalDateTime.now().minusDays(30)
            if (reviewRepository.isReviewEligibleForUpdate(it, before30Days)) {
                review.updateMessageAndScore(dto.message, dto.score)
                return review.id
            }
        }
        throw ReviewNotUpdatableException("It can be modified 30 days after the initial creation.")
    }

    @Transactional(readOnly = true)
    fun findReviewListByMusicId(
        musicId: Long,
        sort: String,
        dto: ReviewCursorRequestDTO,
        pageable: Pageable
    ): Slice<ReviewQueryDTO> = reviewRepository.findReviewsOnMusic(musicId, ReviewSort.of(sort), dto, pageable)

    @Transactional
    fun deleteReview(reviewId: Long, userId: Long) {
        val review = reviewRepository.findReviewByIdAndUserId(reviewId, userId)
            ?: throw ReviewUpdateNotAuthorizedException("Not Authorized for Delete")
        review.id?.let {
            val comments = commentRepository.findCommentsByReview(review)
            comments.forEach { comment -> comment.softDelete() }
            review.softDelete()
        }
    }

    private fun findReviewById(id: Long): Review =
        reviewRepository.findById(id)
            .orElseThrow { ReviewNotFoundException("Review not found: $id") }
}