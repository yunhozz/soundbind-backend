package com.sound_bind.review_service.global.dto.response

import com.querydsl.core.annotations.QueryProjection

data class ReviewLikesQueryDTO @QueryProjection constructor(
    val id: Long,
    val userId: Long,
    val reviewId: Long,
    val flag: Boolean
)
