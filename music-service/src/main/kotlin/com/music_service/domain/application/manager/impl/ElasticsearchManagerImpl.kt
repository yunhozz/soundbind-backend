package com.music_service.domain.application.manager.impl

import com.music_service.domain.application.MusicSearchService
import com.music_service.domain.application.dto.response.MusicDetailsDTO
import com.music_service.domain.application.manager.ElasticsearchManager
import org.springframework.stereotype.Component

@Component
class ElasticsearchManagerImpl(private val musicSearchService: MusicSearchService): ElasticsearchManager {

    override fun onMusicUpload(dto: MusicDetailsDTO) =
        musicSearchService.saveMusicByElasticsearch(dto)

    override fun onMusicDelete(musicId: Long, fileIds: List<Long>) =
        musicSearchService.deleteMusicByElasticsearch(musicId, fileIds)
}