package com.music_service.domain.persistence.repository

import com.music_service.global.dto.response.MusicDetailsQueryDTO

interface MusicQueryRepository {
    fun findMusicDetailsById(id: Long): MusicDetailsQueryDTO?
}