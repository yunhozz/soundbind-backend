package com.music_service.global.dto.response

import com.querydsl.core.annotations.QueryProjection

data class MusicSimpleQueryDTO @QueryProjection constructor(
    val id: Long,
    val userNickname: String,
    val title: String,
    val imageUrl: String
)
