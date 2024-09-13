package com.music_service.domain.application.manager.impl

import com.music_service.domain.application.manager.LockManager
import com.music_service.domain.persistence.entity.Genre
import com.music_service.domain.persistence.entity.Music
import com.music_service.domain.persistence.repository.MusicRepository
import com.music_service.global.annotation.DistributedLock
import com.music_service.global.util.RedisUtils
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class LockManagerImpl(private val musicRepository: MusicRepository): LockManager {

    @DistributedLock(key = "'save-music-lock-' + #userId", leaseTime = 1, timeUnit = TimeUnit.MINUTES)
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
}