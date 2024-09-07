package com.sound_bind.review_service.domain.application

import com.sound_bind.review_service.domain.application.dto.response.CommentDetailsDTO
import com.sound_bind.review_service.domain.application.dto.response.ReviewDetailsDTO
import com.sound_bind.review_service.domain.persistence.es.CommentDocument
import com.sound_bind.review_service.domain.persistence.es.CommentSearchRepository
import com.sound_bind.review_service.domain.persistence.es.ReviewDocument
import com.sound_bind.review_service.domain.persistence.es.ReviewSearchRepository
import com.sound_bind.review_service.domain.persistence.repository.ReviewRepository
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewCursorDTO
import com.sound_bind.review_service.global.enums.ReviewSort
import com.sound_bind.review_service.global.util.DateTimeUtils
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ElasticsearchService(
    private val reviewRepository: ReviewRepository,
    private val reviewSearchRepository: ReviewSearchRepository,
    private val commentSearchRepository: CommentSearchRepository
) {

    @Async
    fun saveReviewInElasticSearch(dto: ReviewDetailsDTO) {
        val reviewDocument = ReviewDocument(
            dto.id,
            dto.musicId,
            dto.userId,
            dto.userNickname,
            dto.userImageUrl,
            dto.message,
            dto.score,
            dto.commentNum,
            dto.likes,
            isLiked = false,
            DateTimeUtils.convertLocalDateTimeToString(dto.createdAt),
            DateTimeUtils.convertLocalDateTimeToString(dto.updatedAt)
        )
        reviewSearchRepository.save(reviewDocument)
    }

    @Async
    fun saveCommentInElasticSearch(dto: CommentDetailsDTO) {
        val commentDocument = CommentDocument(
            dto.id,
            dto.reviewId,
            dto.userId,
            dto.userNickname,
            dto.message,
            DateTimeUtils.convertLocalDateTimeToString(dto.createdAt),
            DateTimeUtils.convertLocalDateTimeToString(dto.updatedAt)
        )
        commentSearchRepository.save(commentDocument)
    }

    @Transactional(readOnly = true)
    fun findReviewListByMusicIdV2(
        musicId: Long,
        userId: Long,
        reviewSort: ReviewSort,
        dto: ReviewCursorDTO?
    ): List<ReviewDocument?> =
        reviewRepository.findReviewsOnMusicWithES(
            musicId,
            userId,
            reviewSort,
            dto
        )

    fun findCommentsByReviewInElasticsearch(reviewId: Long): List<CommentDocument> =
        commentSearchRepository.findByReviewIdOrderByCreatedAtAsc(reviewId)

    @Async
    fun deleteReviewInElasticSearch(reviewId: Long) =
        reviewSearchRepository.deleteById(reviewId)

    @Async
    fun deleteCommentInElasticSearch(commentId: Long) =
        commentSearchRepository.deleteById(commentId)

    @Async
    fun deleteCommentsInElasticSearch(commentIds: List<Long>) =
        commentSearchRepository.deleteAllById(commentIds)

    @Async
    fun deleteReviewsByUserIdInElasticSearch(userId: Long) =
        reviewSearchRepository.deleteAllByUserId(userId)
}