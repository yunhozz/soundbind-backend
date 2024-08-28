package com.sound_bind.review_service.domain.persistence.repository

import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewCursorDTO
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewQueryDTO
import com.sound_bind.review_service.global.enums.ReviewSort
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice

interface ReviewQueryRepository {
    fun findReviewsOnMusic(
        musicId: Long,
        userId: Long,
        sort: ReviewSort,
        dto: ReviewCursorDTO?,
        pageable: Pageable
    ): Slice<ReviewQueryDTO>

    fun addIsLikedToReviewDocuments(reviews: MutableList<ReviewQueryDTO>, userId: Long): MutableList<ReviewQueryDTO>
}