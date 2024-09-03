package com.music_service.domain.persistence.repository.dto

data class MusicCursorDTO(
    val likesCursor: Int,
    val accuracyCursor: Int,
    val createdAtCursor: Int
)