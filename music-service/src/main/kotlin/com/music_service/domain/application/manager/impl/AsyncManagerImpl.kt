package com.music_service.domain.application.manager.impl

import com.music_service.domain.application.MusicSearchService
import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import com.music_service.domain.application.dto.response.MusicDetailsDTO
import com.music_service.domain.application.manager.AsyncManager
import com.music_service.global.config.AsyncConfig.Companion.THREAD_POOL_TASK_EXECUTOR
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class AsyncManagerImpl(
    private val fileManager: FileManagerImpl,
    private val musicSearchService: MusicSearchService
): AsyncManager {

    @Async(THREAD_POOL_TASK_EXECUTOR)
    override fun musicUploadWithAsync(fileInfo: FileUploadResponseDTO) =
        fileManager.onMusicUpload(fileInfo)

    @Async(THREAD_POOL_TASK_EXECUTOR)
    override fun musicUpdateWithAsync(fileUrl: String, dto: FileUploadResponseDTO) =
        fileManager.onMusicUpdate(fileUrl, dto)

    @Async(THREAD_POOL_TASK_EXECUTOR)
    override fun musicDeleteWithAsync(fileUrl: String) =
        fileManager.onMusicDelete(fileUrl)

    @Async(THREAD_POOL_TASK_EXECUTOR)
    override fun saveMusicByElasticsearchWithAsync(dto: MusicDetailsDTO) =
        musicSearchService.saveMusicByElasticsearch(dto)

    @Async(THREAD_POOL_TASK_EXECUTOR)
    override fun deleteMusicByElasticsearchWithAsync(musicId: Long, fileIds: List<Long>) =
        musicSearchService.deleteMusicByElasticsearch(musicId, fileIds)
}