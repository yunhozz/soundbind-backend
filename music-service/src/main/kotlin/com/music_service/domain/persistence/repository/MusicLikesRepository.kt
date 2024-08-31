package com.music_service.domain.persistence.repository

import com.music_service.domain.persistence.entity.MusicLikes
import org.springframework.data.jpa.repository.JpaRepository

interface MusicLikesRepository: JpaRepository<MusicLikes, Long>