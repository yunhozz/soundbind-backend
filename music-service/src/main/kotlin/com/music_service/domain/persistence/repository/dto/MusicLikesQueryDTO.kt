package com.music_service.domain.persistence.repository.dto

import com.querydsl.core.annotations.QueryProjection

data class MusicLikesQueryDTO @QueryProjection constructor(
    val id: Long,
    val userId: Long,
    val musicId: Long,
    val flag: Boolean
)