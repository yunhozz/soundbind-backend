package com.music_service.domain.persistence.repository

import com.music_service.domain.persistence.es.document.MusicDocument
import com.music_service.domain.persistence.repository.dto.MusicCursorDTO

interface MusicQueryRepository {
    fun findMusicSimpleListByKeywordAndCondition(keyword: String, sort: MusicSort, cursor: MusicCursorDTO?, userId: Long): List<MusicDocument?>
    fun addMusicDetailsByDocumentAndUserId(musicDocument: MusicDocument, userId: Long): MusicDocument
}

enum class MusicSort(val key: String, val value: String, val target: String) {
    LIKES("likes", "인기순", "likes"),
    ACCURACY("accuracy", "정확도순", ""),
    LATEST("latest", "최신순", "createdAt")
    ;

    companion object {
        fun of(key: String): MusicSort = entries.find {
            it.key == key
        } ?: throw IllegalArgumentException("Unknown Sorting '$key'")
    }
}