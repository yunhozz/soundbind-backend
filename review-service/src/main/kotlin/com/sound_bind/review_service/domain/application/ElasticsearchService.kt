package com.sound_bind.review_service.domain.application

import com.sound_bind.review_service.domain.application.dto.response.CommentDetailsDTO
import com.sound_bind.review_service.domain.application.dto.response.ReviewDetailsDTO
import com.sound_bind.review_service.domain.persistence.es.CommentDocument
import com.sound_bind.review_service.domain.persistence.es.CommentSearchRepository
import com.sound_bind.review_service.domain.persistence.es.ReviewDocument
import com.sound_bind.review_service.domain.persistence.es.ReviewSearchRepository
import com.sound_bind.review_service.global.util.DateTimeUtils
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class ElasticsearchService(
    private val reviewSearchRepository: ReviewSearchRepository,
    private val commentSearchRepository: CommentSearchRepository
) {

    @Async
    fun indexReviewInElasticSearch(dto: ReviewDetailsDTO) {
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
            DateTimeUtils.convertLocalDateTimeToString(dto.createdAt),
            DateTimeUtils.convertLocalDateTimeToString(dto.updatedAt)
        )
        reviewSearchRepository.save(reviewDocument)
    }

    @Async
    fun indexCommentInElasticSearch(dto: CommentDetailsDTO) {
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

    fun findCommentsByReviewInElasticsearch(reviewId: Long): List<CommentDocument> =
        commentSearchRepository.findByReviewIdOrderByCreatedAtAsc(reviewId)

    @Async
    fun deleteReviewInElasticSearch(reviewId: Long) =
        reviewSearchRepository.deleteById(reviewId)

    @Async
    fun deleteCommentInElasticSearch(commentId: Long) =
        commentSearchRepository.deleteById(commentId)

    @Async
    fun deleteReviewsByUserIdInElasticSearch(userId: Long) =
        reviewSearchRepository.deleteAllByUserId(userId)
}