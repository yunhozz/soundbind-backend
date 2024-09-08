package com.music_service.domain.application.listener

import com.music_service.domain.application.ElasticsearchService
import com.music_service.domain.application.dto.response.MusicDetailsDTO
import org.springframework.stereotype.Component

@Component
class MusicElasticsearchListener(private val elasticsearchService: ElasticsearchService): ElasticsearchListener {

    override fun onMusicUpload(dto: MusicDetailsDTO) =
        elasticsearchService.saveMusicByElasticsearch(dto)

    override fun onMusicDelete(musicId: Long, fileIds: List<Long>) =
        elasticsearchService.deleteMusicByElasticsearch(musicId, fileIds)
}