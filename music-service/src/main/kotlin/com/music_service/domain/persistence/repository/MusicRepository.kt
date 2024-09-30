package com.music_service.domain.persistence.repository

import com.music_service.domain.persistence.entity.Music
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface MusicRepository: JpaRepository<Music, Long>, MusicQueryRepository {

    @Query("select m from Music m where m.id = :id and m.updatedAt < :cutoffDate")
    fun findMusicEligibleForUpdateById(id: Long, cutoffDate: LocalDateTime): Music?

    fun findMusicByUserId(userId: Long): List<Music>
}