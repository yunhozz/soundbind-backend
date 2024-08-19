package com.music_service.domain.persistence.repository.dto

import com.querydsl.core.annotations.QueryProjection

data class MusicSimpleQueryDTO @QueryProjection constructor(
    val id: Long,
    val userNickname: String,
    val title: String,
    val imageUrl: String
)
