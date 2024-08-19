package com.music_service.domain.persistence.repository

import com.music_service.domain.persistence.repository.dto.MusicDetailsQueryDTO
import com.music_service.domain.persistence.repository.dto.MusicSimpleQueryDTO
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice

interface MusicQueryRepository {
    fun findMusicDetailsById(id: Long): MusicDetailsQueryDTO?
    fun findMusicSimpleListByKeyword(keyword: String, pageable: Pageable): Slice<MusicSimpleQueryDTO>
}