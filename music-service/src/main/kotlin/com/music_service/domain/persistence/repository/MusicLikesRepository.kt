package com.music_service.domain.persistence.repository

import com.music_service.domain.persistence.entity.MusicLikes
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MusicLikesRepository: JpaRepository<MusicLikes, Long> {

    @Query("select ml from MusicLikes ml join fetch ml.music m where m.id = :musicId")
    fun findMusicLikesWithMusicByMusicId(musicId: Long): MusicLikes?

    fun findMusicLikesByUserId(userId: Long): List<MusicLikes>
}