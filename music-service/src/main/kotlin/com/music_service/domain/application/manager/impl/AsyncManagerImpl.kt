package com.music_service.domain.application.manager.impl

import com.music_service.domain.application.MusicSearchService
import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import com.music_service.domain.application.dto.response.MusicDetailsDTO
import com.music_service.domain.application.manager.AsyncManager
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class AsyncManagerImpl(
    private val fileManager: FileManagerImpl,
    private val musicSearchService: MusicSearchService
): AsyncManager {

    @Async
    override fun musicUploadWithAsync(fileInfo: FileUploadResponseDTO) =
        fileManager.onMusicUpload(fileInfo)

    @Async
    override fun musicUpdateWithAsync(fileUrl: String, dto: FileUploadResponseDTO) =
        fileManager.onMusicUpdate(fileUrl, dto)

    @Async
    override fun musicDeleteWithAsync(fileUrl: String) =
        fileManager.onMusicDelete(fileUrl)

    @Async
    override fun saveMusicByElasticsearchWithAsync(dto: MusicDetailsDTO) =
        musicSearchService.saveMusicByElasticsearch(dto)

    @Async
    override fun deleteMusicByElasticsearchWithAsync(musicId: Long, fileIds: List<Long>) =
        musicSearchService.deleteMusicByElasticsearch(musicId, fileIds)
}