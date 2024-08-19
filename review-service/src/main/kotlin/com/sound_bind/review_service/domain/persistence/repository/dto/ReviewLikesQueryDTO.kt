package com.sound_bind.review_service.domain.persistence.repository.dto

import com.querydsl.core.annotations.QueryProjection

data class ReviewLikesQueryDTO @QueryProjection constructor(
    val id: Long,
    val userId: Long,
    val reviewId: Long,
    val flag: Boolean
)
