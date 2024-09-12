package com.music_service.domain.application.manager

import com.music_service.domain.application.dto.response.MusicDetailsDTO

interface ElasticsearchManager {
    fun onMusicUpload(dto: MusicDetailsDTO)
    fun onMusicDelete(musicId: Long, fileIds: List<Long>)
}