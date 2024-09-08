package com.music_service.domain.application.listener

import com.music_service.domain.application.dto.response.MusicDetailsDTO

interface ElasticsearchListener {
    fun onMusicUpload(dto: MusicDetailsDTO)
    fun onMusicDelete(musicId: Long, fileIds: List<Long>)
}