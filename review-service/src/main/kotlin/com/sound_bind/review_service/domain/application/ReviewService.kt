package com.sound_bind.review_service.domain.application

import com.sound_bind.global.utils.KafkaConstants
import com.sound_bind.review_service.domain.application.dto.message.MusicNotFoundMessageDTO
import com.sound_bind.review_service.domain.application.dto.message.UserWithdrawMessageDTO
import com.sound_bind.review_service.domain.application.dto.request.ReviewCreateDTO
import com.sound_bind.review_service.domain.application.dto.request.ReviewUpdateDTO
import com.sound_bind.review_service.domain.application.dto.response.ReviewDetailsDTO
import com.sound_bind.review_service.domain.application.manager.KafkaManager
import com.sound_bind.review_service.domain.application.manager.ReviewElasticsearchManager
import com.sound_bind.review_service.domain.persistence.entity.Review
import com.sound_bind.review_service.domain.persistence.entity.ReviewLikes
import com.sound_bind.review_service.domain.persistence.repository.CommentRepository
import com.sound_bind.review_service.domain.persistence.repository.ReviewLikesRepository
import com.sound_bind.review_service.domain.persistence.repository.ReviewRepository
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewCursorDTO
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewQueryDTO
import com.sound_bind.review_service.global.annotation.DistributedLock
import com.sound_bind.review_service.global.enums.ReviewSort
import com.sound_bind.review_service.global.exception.ReviewServiceException.NegativeValueException
import com.sound_bind.review_service.global.exception.ReviewServiceException.ReviewAlreadyExistException
import com.sound_bind.review_service.global.exception.ReviewServiceException.ReviewNotFoundException
import com.sound_bind.review_service.global.exception.ReviewServiceException.ReviewNotUpdatableException
import com.sound_bind.review_service.global.exception.ReviewServiceException.ReviewUpdateNotAuthorizedException
import com.sound_bind.review_service.global.util.RedisUtils
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val commentRepository: CommentRepository,
    private val reviewLikesRepository: ReviewLikesRepository,
    private val elasticsearchManager: ReviewElasticsearchManager,
    private val kafkaManager: KafkaManager
) {

    @Transactional
    @DistributedLock(key = "'create-review-lock-' + #musicId + '-' + #userId")
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

        elasticsearchManager.onReviewCreate(ReviewDetailsDTO(review))
        kafkaManager.sendMusicReviewCreateTopic(
            musicId,
            review.id!!,
            reviewerId = userId,
            nickname = userInfo["nickname"] as String,
            oldScore = null,
            score = dto.score
        )

        return review.id!!
    }

    @Transactional
    @KafkaListener(groupId = KafkaConstants.REVIEW_SERVICE_GROUP, topics = [KafkaConstants.REVIEW_ROLLBACK_TOPIC])
    fun createReviewRollback(@Payload payload: MusicNotFoundMessageDTO) {
        val reviewId = payload.reviewId
        val review = findReviewById(reviewId)

        review.softDelete()
        elasticsearchManager.onReviewDelete(reviewId, emptyList())
        kafkaManager.sendMusicNotFoundTopic(payload.reviewerId,
            "music id = ${payload.musicId} 에 해당하는 음원이 존재하지 않아 리뷰가 삭제되었습니다.")
    }

    @Transactional
    @DistributedLock(key = "'update-review-lock-' + #reviewId + '-' + #userId", waitTime = 0)
    fun updateReviewMessageAndScore(reviewId: Long, userId: Long, dto: ReviewUpdateDTO): Long {
        val review = reviewRepository.findReviewByIdAndUserId(reviewId, userId)
            ?: throw ReviewUpdateNotAuthorizedException("Not Authorized for Update")
        val oldScore = review.score
        val before30Days = LocalDateTime.now()

        if (reviewRepository.isReviewEligibleForUpdate(review.id!!, before30Days)) {
            val newScore = dto.score
            review.updateMessageAndScore(dto.message, newScore)
            elasticsearchManager.onReviewCreate(ReviewDetailsDTO(review))
            kafkaManager.sendMusicReviewUpdateTopic(
                review.musicId,
                review.id!!,
                reviewerId = userId,
                oldScore,
                newScore
            )

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
        reviewRepository.findReviewsOnMusic(musicId, userId, reviewSort, dto, pageable)

    @Transactional
    @DistributedLock(key = "'change-review-likes-flag-lock-' + #reviewId")
    fun changeLikesFlag(reviewId: Long, userId: Long) {
        var reviewerId: Long? = null
        reviewLikesRepository.findWithReviewByReviewIdAndUserId(reviewId, userId)?.let { rl ->
            try {
                rl.changeFlag() // change review's likes number
                if (rl.flag) reviewerId = rl.review.userId
            } catch (e: IllegalArgumentException) {
                throw NegativeValueException(e.localizedMessage)
            }
        } ?: run {
            val review = findReviewById(reviewId)
            val reviewLikes = ReviewLikes(userId, review)
            reviewLikesRepository.save(reviewLikes)
            review.addLikes(1)
            reviewerId = review.userId
        }
        reviewerId?.let {
            val myInfo = getUserInformationOnRedis(userId)
            kafkaManager.sendReviewLikeTopic(it,
                "${myInfo["nickname"] as String} 님이 당신의 리뷰에 좋아요를 눌렀습니다.")
        }
    }

    @Transactional
    fun deleteReview(reviewId: Long, userId: Long) {
        val review = reviewRepository.findReviewByIdAndUserId(reviewId, userId)
            ?: throw ReviewUpdateNotAuthorizedException("You're not Authorized for Delete Review")
        val comments = commentRepository.findCommentsByReview(review)
        val reviewLikesList = reviewLikesRepository.findByReview(review)

        kafkaManager.sendMusicReviewUpdateTopic(
            musicId = review.musicId,
            review.id!!,
            reviewerId = userId,
            oldScore = null,
            score = review.score.unaryMinus()
        )

        review.softDelete()
        comments.forEach { it.softDelete() }
        reviewLikesRepository.deleteAllInBatch(reviewLikesList)

        val commentIds = comments.map { it.id!! }
        elasticsearchManager.onReviewDelete(review.id!!, commentIds)
    }

    @Transactional
    @KafkaListener(groupId = KafkaConstants.REVIEW_SERVICE_GROUP, topics = [KafkaConstants.USER_DELETION_TOPIC])
    fun deleteReviewsByUserWithdraw(@Payload payload: UserWithdrawMessageDTO) {
        val userId = payload.userId
        val reviews = reviewRepository.findReviewsByUserId(userId)

        reviews.forEach { review ->
            val comments = commentRepository.findCommentsByReview(review)
            val reviewLikesList = reviewLikesRepository.findByReview(review)

            comments.forEach { it.softDelete() }
            reviewLikesRepository.deleteAllInBatch(reviewLikesList)
            review.softDelete()
        }
        elasticsearchManager.onReviewsDeleteByUserWithdraw(userId)
        reviewRepository.deleteReviewsByUserId(LocalDateTime.now(), userId)
    }

    private fun getUserInformationOnRedis(userId: Long) =
        RedisUtils.getJson("user:$userId", Map::class.java)
            ?: throw IllegalArgumentException("Value is not Present by Key : user:$userId")

    private fun findReviewById(id: Long): Review =
        reviewRepository.findById(id)
            .orElseThrow { ReviewNotFoundException("Review not found: $id") }
}