package com.music_service.domain.persistence.repository

import com.music_service.domain.persistence.entity.MusicLikes
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface MusicLikesRepository: JpaRepository<MusicLikes, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ml from MusicLikes ml join fetch ml.music m where m.id = :musicId and ml.userId = :userId")
    fun findMusicLikesWithMusicByMusicIdAndUserId(musicId: Long, userId: Long): MusicLikes?

    fun findMusicLikesByUserId(userId: Long): List<MusicLikes>

    fun findMusicLikesByFlag(flag: Boolean): List<MusicLikes>
}