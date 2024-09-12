package com.music_service.domain.application.manager.impl

import com.music_service.domain.application.ElasticsearchService
import com.music_service.domain.application.dto.response.MusicDetailsDTO
import com.music_service.domain.application.manager.ElasticsearchManager
import org.springframework.stereotype.Component

@Component
class ElasticsearchManagerImpl(private val elasticsearchService: ElasticsearchService): ElasticsearchManager {

    override fun onMusicUpload(dto: MusicDetailsDTO) =
        elasticsearchService.saveMusicByElasticsearch(dto)

    override fun onMusicDelete(musicId: Long, fileIds: List<Long>) =
        elasticsearchService.deleteMusicByElasticsearch(musicId, fileIds)
}