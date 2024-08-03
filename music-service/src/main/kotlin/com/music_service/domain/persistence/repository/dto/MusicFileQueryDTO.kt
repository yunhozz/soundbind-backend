package com.music_service.domain.persistence.repository.dto

import com.querydsl.core.annotations.QueryProjection

data class MusicFileQueryDTO @QueryProjection constructor(
    val id: Long,
    val originalFileName: String,
    val savedName: String,
    val fileUrl: String
)
