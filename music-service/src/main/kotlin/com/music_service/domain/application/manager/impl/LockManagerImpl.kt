package com.music_service.domain.application.manager.impl

import com.music_service.domain.application.manager.LockManager
import com.music_service.domain.persistence.entity.Genre
import com.music_service.domain.persistence.entity.Music
import com.music_service.domain.persistence.entity.MusicLikes
import com.music_service.domain.persistence.repository.MusicLikesRepository
import com.music_service.domain.persistence.repository.MusicRepository
import com.music_service.global.annotation.DistributedLock
import com.music_service.global.exception.MusicServiceException.MusicNotFoundException
import com.music_service.global.util.RedisUtils
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class LockManagerImpl(
    private val musicRepository: MusicRepository,
    private val musicLikesRepository: MusicLikesRepository
): LockManager {

    @DistributedLock(
        key = "'save-music-lock-' + #userId",
        leaseTime = 1
    )
    override fun saveMusicWithLock(userId: Long, title: String, genres: Set<String>): Music {
        val userInfo = RedisUtils.getJson("user:$userId", Map::class.java)
        val music = Music.create(
            userId,
            userInfo["nickname"] as String,
            title,
            genres.map { Genre.of(it) }.toSet()
        )
        return musicRepository.save(music)
    }

    @DistributedLock(
        key = "'change-likes-flag-lock-' + #musicId",
        leaseTime = 500,
        timeUnit = TimeUnit.MILLISECONDS,
        retryCount = 10,
        retryTimeMillis = 1500
    )
    override fun changeLikesFlagWithLock(musicId: Long, userId: Long): Long? {
        var result: Long? = null
        musicLikesRepository.findMusicLikesWithMusicByMusicIdAndUserId(musicId, userId)?.let { ml ->
            ml.changeFlag()
            if (ml.flag) result = ml.music.userId
        } ?: run {
            val music = musicRepository.findMusicByIdWithLock(musicId)
                ?: throw MusicNotFoundException("Music not found with id: $musicId")
            val musicLikes = MusicLikes(music, userId)
            musicLikesRepository.save(musicLikes)
            music.addLikes(1)
            result = music.userId
        }
        return result
    }
}