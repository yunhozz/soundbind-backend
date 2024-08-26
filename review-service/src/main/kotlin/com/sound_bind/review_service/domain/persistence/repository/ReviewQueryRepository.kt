package com.sound_bind.review_service.domain.persistence.repository

import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewCursorDTO
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewQueryDTO
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice

interface ReviewQueryRepository {
    fun findReviewsOnMusic(musicId: Long, userId: Long, sort: ReviewSort, dto: ReviewCursorDTO?, pageable: Pageable): Slice<ReviewQueryDTO>
    fun findReviewsOnMusicWithElasticsearch(musicId: Long, userId: Long, sort: ReviewSort, dto: ReviewCursorDTO?, pageable: Pageable): List<ReviewQueryDTO>
}

enum class ReviewSort(val key: String, val value: String, val target: String) {
    LIKES("likes", "인기순", "likes"),
    LATEST("latest", "최신순", "createdAt")
    ;

    companion object {
        fun of(key: String): ReviewSort = entries.find {
            it.key == key
        } ?: throw IllegalArgumentException("Unknown Sorting '$key'")
    }
}