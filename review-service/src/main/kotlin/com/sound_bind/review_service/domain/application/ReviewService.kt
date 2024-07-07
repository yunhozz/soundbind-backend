package com.sound_bind.review_service.domain.application

import com.sound_bind.review_service.domain.interfaces.handler.ReviewServiceException.ReviewAlreadyExistException
import com.sound_bind.review_service.domain.persistence.entity.Review
import com.sound_bind.review_service.domain.persistence.repository.ReviewRepository
import com.sound_bind.review_service.global.dto.request.ReviewCreateDTO
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
}