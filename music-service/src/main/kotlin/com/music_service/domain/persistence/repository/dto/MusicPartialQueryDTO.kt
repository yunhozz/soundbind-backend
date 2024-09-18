package com.music_service.domain.persistence.repository.dto

import com.querydsl.core.annotations.QueryProjection

data class MusicPartialQueryDTO @QueryProjection constructor(
    val id: Long,
    val likes: Int,
    val scoreAverage: Double
)