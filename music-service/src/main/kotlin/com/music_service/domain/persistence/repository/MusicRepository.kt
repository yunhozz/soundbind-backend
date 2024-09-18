package com.music_service.domain.persistence.repository

import com.music_service.domain.persistence.entity.Music
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface MusicRepository: JpaRepository<Music, Long>, MusicQueryRepository {

    @Query("select m from Music m where m.id = :id and m.updatedAt < :cutoffDate")
    fun findMusicEligibleForUpdateById(id: Long, cutoffDate: LocalDateTime): Music?

    @Query("select m.userId from Music m where m.id = :id")
    fun findMusicianIdById(id: Long): Long?

    fun findMusicByUserId(userId: Long): List<Music>

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select m from Music m where m.id = :musicId")
    fun findMusicByIdWithLock(musicId: Long): Music?
}