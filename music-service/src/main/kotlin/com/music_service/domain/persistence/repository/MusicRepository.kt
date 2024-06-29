package com.music_service.domain.persistence.repository

import com.music_service.domain.persistence.entity.Music
import org.springframework.data.jpa.repository.JpaRepository

interface MusicRepository: JpaRepository<Music, Long>