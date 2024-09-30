package com.music_service.domain.application.manager

interface CacheManager {
    fun clearMusicSimpleSearchResultsCache()
    fun clearMusicDetailsCache(musicId: Long)
}