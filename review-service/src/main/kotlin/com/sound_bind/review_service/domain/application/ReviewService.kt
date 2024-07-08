package com.sound_bind.review_service.domain.application

import com.sound_bind.review_service.domain.interfaces.handler.ReviewServiceException.ReviewAlreadyExistException
import com.sound_bind.review_service.domain.interfaces.handler.ReviewServiceException.ReviewNotFoundException
import com.sound_bind.review_service.domain.interfaces.handler.ReviewServiceException.ReviewNotUpdatableException
import com.sound_bind.review_service.domain.persistence.entity.Review
import com.sound_bind.review_service.domain.persistence.repository.ReviewRepository
import com.sound_bind.review_service.global.dto.request.ReviewCreateDTO
import com.sound_bind.review_service.global.dto.request.ReviewUpdateDTO
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository
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
    fun updateReviewMessageAndScore(reviewId: Long, dto: ReviewUpdateDTO): Long? {
        val review = reviewRepository.findById(reviewId)
            .orElseThrow { ReviewNotFoundException("Review not found: $reviewId") }
        review.id?.let {
            if (reviewRepository.isReviewEligibleForUpdate(it)) {
                review.updateMessageAndScore(dto.message, dto.score)
                return review.id
            }
        }
        throw ReviewNotUpdatableException("It can be modified 30 days after the initial creation.")
    }
}