package com.sound_bind.review_service.domain.application

import com.sound_bind.review_service.domain.application.dto.request.ReviewCreateDTO
import com.sound_bind.review_service.domain.application.dto.request.ReviewUpdateDTO
import com.sound_bind.review_service.domain.application.dto.response.ReviewDetailsDTO
import com.sound_bind.review_service.domain.application.listener.ReviewElasticsearchListener
import com.sound_bind.review_service.domain.persistence.entity.Review
import com.sound_bind.review_service.domain.persistence.entity.ReviewLikes
import com.sound_bind.review_service.domain.persistence.repository.CommentRepository
import com.sound_bind.review_service.domain.persistence.repository.ReviewLikesRepository
import com.sound_bind.review_service.domain.persistence.repository.ReviewRepository
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewCursorDTO
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewQueryDTO
import com.sound_bind.review_service.global.enums.ReviewSort
import com.sound_bind.review_service.global.exception.ReviewServiceException.NegativeValueException
import com.sound_bind.review_service.global.exception.ReviewServiceException.ReviewAlreadyExistException
import com.sound_bind.review_service.global.exception.ReviewServiceException.ReviewNotFoundException
import com.sound_bind.review_service.global.exception.ReviewServiceException.ReviewNotUpdatableException
import com.sound_bind.review_service.global.exception.ReviewServiceException.ReviewUpdateNotAuthorizedException
import com.sound_bind.review_service.global.util.RedisUtils
import org.springframework.beans.factory.annotation.Autowired
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

    @Autowired
    private lateinit var elasticsearchListener: ReviewElasticsearchListener

    @Transactional
    fun createReview(musicId: Long, userId: Long, dto: ReviewCreateDTO): Long {
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
        elasticsearchListener.onReviewCreate(ReviewDetailsDTO(review))

        return review.id!!
    }

    @Transactional
    fun updateReviewMessageAndScore(reviewId: Long, userId: Long, dto: ReviewUpdateDTO): Long {
        val review = reviewRepository.findReviewByIdAndUserId(reviewId, userId)
            ?: throw ReviewUpdateNotAuthorizedException("Not Authorized for Update")
        val before30Days = LocalDateTime.now().minusDays(30)
        if (reviewRepository.isReviewEligibleForUpdate(review.id!!, before30Days)) {
            review.updateMessageAndScore(dto.message, dto.score)
            elasticsearchListener.onReviewCreate(ReviewDetailsDTO(review))
            return review.id!!
        } else {
            throw ReviewNotUpdatableException("It can be modified after 30 days of final modification.")
        }
    }

    @Transactional(readOnly = true)
    fun lookupDetailsOfReviewById(reviewId: Long): ReviewDetailsDTO {
        val review = findReviewById(reviewId)
        return ReviewDetailsDTO(review)
    }

    @Transactional(readOnly = true)
    fun findReviewListByMusicIdV1(
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
    fun deleteReview(reviewId: Long, userId: Long) {
        val review = reviewRepository.findReviewByIdAndUserId(reviewId, userId)
            ?: throw ReviewUpdateNotAuthorizedException("Not Authorized for Delete")
        val comments = commentRepository.findCommentsByReview(review)
        val reviewLikesList = reviewLikesRepository.findByReview(review)

        comments.forEach { it.softDelete() }
        reviewLikesRepository.deleteAllInBatch(reviewLikesList)
        review.softDelete()

        val commentIds = comments.map { it.id!! }
        elasticsearchListener.onReviewDelete(review.id!!, commentIds)
    }

    @Transactional
    fun deleteReviewsByUserWithdraw(userId: Long) {
        val reviews = reviewRepository.findReviewsByUserId(userId)
        reviews.forEach { review ->
            val comments = commentRepository.findCommentsByReview(review)
            val reviewLikesList = reviewLikesRepository.findByReview(review)

            comments.forEach { it.softDelete() }
            reviewLikesRepository.deleteAllInBatch(reviewLikesList)
            review.softDelete()
        }
        reviewRepository.deleteReviewsByUserId(LocalDateTime.now(), userId)
        elasticsearchListener.onReviewsDeleteByUserWithdraw(userId)
    }

    fun getUserInformationOnRedis(userId: Long) =
        RedisUtils.getJson("user:$userId", Map::class.java)
            ?: throw IllegalArgumentException("Value is not Present by Key : user:$userId")

    private fun findReviewById(id: Long): Review =
        reviewRepository.findById(id)
            .orElseThrow { ReviewNotFoundException("Review not found: $id") }
}