package com.sound_bind.review_service.domain.persistence.es

import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewCursorDTO
import com.sound_bind.review_service.global.enums.ReviewSort
import org.springframework.data.domain.Pageable

interface ReviewSearchQueryRepository {
    fun findReviewsOnMusicWithElasticsearch(
        musicId: Long,
        userId: Long,
        sort: ReviewSort,
        dto: ReviewCursorDTO?,
        pageable: Pageable
    ): List<ReviewDocument?>
}