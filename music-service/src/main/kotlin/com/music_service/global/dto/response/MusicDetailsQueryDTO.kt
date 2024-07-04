package com.music_service.global.dto.response

import com.music_service.domain.persistence.entity.Music
import com.querydsl.core.annotations.QueryProjection
import java.time.LocalDateTime

data class MusicDetailsQueryDTO(
    val id: Long,
    val userId: Long,
    val userNickname: String,
    val title: String,
    val genres: Set<Music.Genre>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    lateinit var files: List<MusicFileQueryDTO>

    data class MusicFileQueryDTO @QueryProjection constructor(
        val id: Long,
        val originalFileName: String,
        val savedName: String,
        val fileUrl: String
    )
}