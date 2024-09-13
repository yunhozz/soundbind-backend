package com.music_service.domain.application.manager

import com.music_service.domain.persistence.entity.Music

interface LockManager {
    fun saveMusicWithLock(userId: Long, title: String, genres: Set<String>): Music
}