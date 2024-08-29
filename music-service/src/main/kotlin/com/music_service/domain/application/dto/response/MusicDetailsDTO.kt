package com.music_service.domain.application.dto.response

import com.music_service.domain.persistence.entity.FileEntity
import com.music_service.domain.persistence.entity.Music
import com.music_service.domain.persistence.es.MusicDocument

data class MusicDetailsDTO private constructor(
    val id: Long?,
    val userId: Long,
    val userNickname: String,
    val title: String,
    val genres: Set<String>,
    val files: List<FileDetailsDTO>
) {
    constructor(music: Music, files: List<FileEntity>): this(
        music.id,
        music.userId,
        music.userNickname,
        music.title,
        music.genres.map { it.genreName }.toSet(),
        files.map { file -> FileDetailsDTO(file, music.id!!) }
    )

    constructor(musicDocument: MusicDocument, files: List<FileDetailsDTO>): this(
        musicDocument.id!!,
        musicDocument.userId,
        musicDocument.userNickname,
        musicDocument.title,
        musicDocument.genres,
        files
    )
}