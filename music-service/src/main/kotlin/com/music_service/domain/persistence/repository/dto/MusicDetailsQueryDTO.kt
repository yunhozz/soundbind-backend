package com.music_service.domain.persistence.repository.dto

import com.music_service.domain.persistence.entity.Genre
import java.time.LocalDateTime

data class MusicDetailsQueryDTO(
    val id: Long,
    val userId: Long,
    val userNickname: String,
    val title: String,
    val genres: Set<Genre>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    lateinit var files: List<MusicFileQueryDTO>
}