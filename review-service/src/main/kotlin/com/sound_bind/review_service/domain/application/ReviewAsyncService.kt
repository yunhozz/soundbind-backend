package com.sound_bind.review_service.domain.application

import com.sound_bind.review_service.domain.application.dto.response.ReviewDetailsDTO
import com.sound_bind.review_service.domain.persistence.es.ReviewDocument
import com.sound_bind.review_service.domain.persistence.es.ReviewSearchRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class ReviewAsyncService(private val reviewSearchRepository: ReviewSearchRepository) {

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
            convertLocalDateTimeToString(dto.createdAt),
            convertLocalDateTimeToString(dto.updatedAt)
        )
        reviewSearchRepository.save(reviewDocument)
    }

    @Async
    fun deleteReviewInElasticSearch(reviewId: Long) =
        reviewSearchRepository.deleteById(reviewId)

    @Async
    fun deleteReviewsByUserIdInElasticSearch(userId: Long) =
        reviewSearchRepository.deleteAllByUserId(userId)

    companion object {
        private fun convertLocalDateTimeToString(time: LocalDateTime): String =
            time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"))
    }
}