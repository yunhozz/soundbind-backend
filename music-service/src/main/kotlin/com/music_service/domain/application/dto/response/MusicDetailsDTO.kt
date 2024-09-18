package com.music_service.domain.application.dto.response

import com.music_service.domain.persistence.entity.FileEntity
import com.music_service.domain.persistence.entity.Music
import java.time.LocalDateTime

data class MusicDetailsDTO private constructor(
    val id: Long?,
    val userId: Long,
    val userNickname: String,
    val title: String,
    val genres: Set<String>,
    val likes: Int,
    val scoreAverage: Double,
    val files: List<FileDetailsDTO>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    constructor(music: Music, files: List<FileEntity>): this(
        music.id,
        music.userId,
        music.userNickname,
        music.title,
        music.genres.map { it.genreName }.toSet(),
        music.likes,
        music.scoreAverage,
        files.map { file -> FileDetailsDTO(file, music.id!!) },
        music.createdAt,
        music.updatedAt
    )
}