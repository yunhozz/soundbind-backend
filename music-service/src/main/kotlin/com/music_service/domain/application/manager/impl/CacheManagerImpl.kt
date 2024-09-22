package com.music_service.domain.application.manager.impl

import com.music_service.domain.application.manager.CacheManager
import com.music_service.global.config.CacheConfig.Companion.FIVE_MIN_CACHE
import com.music_service.global.config.CacheConfig.Companion.ONE_MIN_CACHE
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Component

@Component
class CacheManagerImpl: CacheManager {

    @CacheEvict(cacheNames = [ONE_MIN_CACHE], allEntries = true)
    override fun clearMusicSimpleSearchResultsCache() {
        println("Cache Clear!!")
    }

    @CacheEvict(cacheNames = [FIVE_MIN_CACHE], key = "'find-music-details-' + #musicId")
    override fun clearMusicDetailsCache(musicId: Long) {
        println("Cache Clear!!")
    }
}