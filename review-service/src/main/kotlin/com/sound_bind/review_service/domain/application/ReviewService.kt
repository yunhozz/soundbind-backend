package com.sound_bind.review_service.domain.application

import com.sound_bind.review_service.domain.application.dto.request.ReviewCreateDTO
import com.sound_bind.review_service.domain.application.dto.request.ReviewUpdateDTO
import com.sound_bind.review_service.domain.application.dto.response.ReviewDetailsDTO
import com.sound_bind.review_service.domain.persistence.entity.Review
import com.sound_bind.review_service.domain.persistence.entity.ReviewLikes
import com.sound_bind.review_service.domain.persistence.repository.CommentRepository
import com.sound_bind.review_service.domain.persistence.repository.ReviewLikesRepository
import com.sound_bind.review_service.domain.persistence.repository.ReviewRepository
import com.sound_bind.review_service.domain.persistence.repository.ReviewSort
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewCursorDTO
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewQueryDTO
import com.sound_bind.review_service.global.exception.ReviewServiceException.NegativeValueException
import com.sound_bind.review_service.global.exception.ReviewServiceException.ReviewAlreadyExistException
import com.sound_bind.review_service.global.exception.ReviewServiceException.ReviewNotFoundException
import com.sound_bind.review_service.global.exception.ReviewServiceException.ReviewNotUpdatableException
import com.sound_bind.review_service.global.exception.ReviewServiceException.ReviewUpdateNotAuthorizedException
import com.sound_bind.review_service.global.util.RedisUtils
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val commentRepository: CommentRepository,
    private val reviewLikesRepository: ReviewLikesRepository
) {

    @Transactional
    fun createReview(musicId: Long, userId: Long, dto: ReviewCreateDTO): ReviewDetailsDTO {
        if (reviewRepository.existsReviewByMusicIdAndUserId(musicId, userId)) {
            throw ReviewAlreadyExistException("Review already exists")
        }
        val userInfo = getUserInformationOnRedis(userId)
        val review = Review.create(
            musicId,
            userId,
            userInfo["nickname"] as String,
            userInfo["profileUrl"] as? String,
            dto.message,
            dto.score
        )
        reviewRepository.save(review)
        return ReviewDetailsDTO(review)
    }

    @Transactional
    fun updateReviewMessageAndScore(reviewId: Long, userId: Long, dto: ReviewUpdateDTO): ReviewDetailsDTO? {
        val review = reviewRepository.findReviewByIdAndUserId(reviewId, userId)
            ?: throw ReviewUpdateNotAuthorizedException("Not Authorized for Update")
        review.id?.let {
            val before30Days = LocalDateTime.now().minusDays(30)
            if (reviewRepository.isReviewEligibleForUpdate(it, before30Days)) {
                review.updateMessageAndScore(dto.message, dto.score)
                return ReviewDetailsDTO(review)
            }
        }
        throw ReviewNotUpdatableException("It can be modified 30 days after the initial creation.")
    }

    @Transactional(readOnly = true)
    fun lookupDetailsOfReviewById(reviewId: Long): ReviewDetailsDTO {
        val review = findReviewById(reviewId)
        return ReviewDetailsDTO(review)
    }

    @Transactional(readOnly = true)
    fun findReviewListByMusicId(
        musicId: Long,
        userId: Long,
        reviewSort: ReviewSort,
        dto: ReviewCursorDTO?,
        pageable: Pageable
    ): Slice<ReviewQueryDTO> =
        reviewRepository.findReviewsOnMusic(
            musicId,
            userId,
            reviewSort,
            dto,
            pageable
        )

    @Transactional
    fun changeLikesFlag(reviewId: Long, userId: Long): Long? {
        reviewLikesRepository.findWithReviewByReviewId(reviewId)?.let { rl ->
            try {
                rl.changeFlag() // change review's likes number
                if (rl.flag) return rl.review.userId
            } catch (e: IllegalArgumentException) {
                throw NegativeValueException(e.localizedMessage)
            }
            return null
        } ?: run {
            val review = findReviewById(reviewId).also { review ->
                val reviewLikes = ReviewLikes(userId, review)
                reviewLikesRepository.save(reviewLikes)
                review.addLikes(1)
            }
            return review.userId
        }
    }

    @Transactional
    fun deleteReview(reviewId: Long, userId: Long): Long {
        val review = reviewRepository.findReviewByIdAndUserId(reviewId, userId)
            ?: throw ReviewUpdateNotAuthorizedException("Not Authorized for Delete")
        val comments = commentRepository.findCommentsByReview(review)
        val reviewLikesList = reviewLikesRepository.findByReview(review)

        comments.forEach { it.softDelete() }
        review.softDelete()
        reviewLikesRepository.deleteAllInBatch(reviewLikesList)

        return reviewId
    }

    @Transactional
    fun deleteReviewsByUserWithdraw(userId: String) =
        reviewRepository.deleteReviewsByUserId(LocalDateTime.now(), userId.toLong())

    fun getUserInformationOnRedis(userId: Long) =
        RedisUtils.getJson("user:$userId", Map::class.java)
            ?: throw IllegalArgumentException("Value is not Present by Key : user:$userId")

    private fun findReviewById(id: Long): Review =
        reviewRepository.findById(id)
            .orElseThrow { ReviewNotFoundException("Review not found: $id") }
}