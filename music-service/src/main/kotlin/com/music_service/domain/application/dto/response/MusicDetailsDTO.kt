package com.music_service.domain.application.dto.response

import com.music_service.domain.persistence.entity.FileEntity
import com.music_service.domain.persistence.entity.Music

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

    data class FileDetailsDTO private constructor(
        val id: Long?,
        val musicId: Long,
        val fileType: String,
        val originalFileName: String,
        val savedName: String,
        val fileUrl: String
    ) {
        constructor(file: FileEntity, musicId: Long): this(
            file.id,
            musicId,
            file.fileType.toString(),
            file.originalFileName,
            file.savedName,
            file.fileUrl
        )
    }
}