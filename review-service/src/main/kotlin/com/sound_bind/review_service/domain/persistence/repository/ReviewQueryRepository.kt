package com.sound_bind.review_service.domain.persistence.repository

import com.sound_bind.review_service.global.dto.request.ReviewCursorRequestDTO
import com.sound_bind.review_service.global.dto.response.ReviewQueryDTO
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice

interface ReviewQueryRepository {

    fun findReviewsOnMusic(musicId: Long, sort: ReviewSort, dto: ReviewCursorRequestDTO, pageable: Pageable): Slice<ReviewQueryDTO>

    enum class ReviewSort(val key: String, val value: String) {
        LIKES("likes", "인기순"),
        LATEST("latest", "최신순")
        ;

        companion object {
            fun of(key: String): ReviewSort = entries.find {
                it.key == key
            } ?: throw IllegalArgumentException("Unknown Sorting '$key'")
        }
    }
}