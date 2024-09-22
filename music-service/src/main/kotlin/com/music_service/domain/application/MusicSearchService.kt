package com.music_service.domain.application

import com.music_service.domain.application.dto.response.FileDetailsDTO
import com.music_service.domain.application.dto.response.MusicDetailsDTO
import com.music_service.domain.application.dto.response.MusicDocumentResponseDTO
import com.music_service.domain.application.dto.response.MusicSimpleResponseDTO
import com.music_service.domain.persistence.es.document.FileDocument
import com.music_service.domain.persistence.es.document.MusicDocument
import com.music_service.domain.persistence.es.search.FileSearchRepository
import com.music_service.domain.persistence.es.search.MusicSearchRepository
import com.music_service.domain.persistence.repository.MusicRepository
import com.music_service.domain.persistence.repository.MusicSort
import com.music_service.domain.persistence.repository.dto.MusicCursorDTO
import com.music_service.global.config.CacheConfig.Companion.FIVE_MIN_CACHE
import com.music_service.global.config.CacheConfig.Companion.ONE_MIN_CACHE
import com.music_service.global.exception.MusicServiceException.MusicNotFoundException
import com.music_service.global.util.DateTimeUtils
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MusicSearchService(
    private val musicRepository: MusicRepository,
    private val musicSearchRepository: MusicSearchRepository,
    private val fileSearchRepository: FileSearchRepository
) {

    fun saveMusicByElasticsearch(dto: MusicDetailsDTO) {
        val fileDocumentList = dto.files.map {
            FileDocument(
                it.id,
                it.musicId,
                it.fileType,
                it.originalFileName,
                it.savedName,
                it.fileUrl,
                DateTimeUtils.convertLocalDateTimeToString(it.createdAt),
                DateTimeUtils.convertLocalDateTimeToString(it.updatedAt)
            )
        }
        val musicDocument = MusicDocument(
            dto.id,
            dto.userId,
            dto.userNickname,
            dto.title,
            dto.genres,
            dto.likes,
            dto.scoreAverage,
            isLiked = false,
            fileDocumentList,
            DateTimeUtils.convertLocalDateTimeToString(dto.createdAt),
            DateTimeUtils.convertLocalDateTimeToString(dto.updatedAt)
        )
        fileSearchRepository.saveAll(fileDocumentList)
        musicSearchRepository.save(musicDocument)
    }

    fun deleteMusicByElasticsearch(musicId: Long, fileIds: List<Long>) {
        musicSearchRepository.deleteById(musicId)
        fileSearchRepository.deleteAllById(fileIds)
    }

    @Transactional(readOnly = true)
    @Cacheable(
        cacheNames = [ONE_MIN_CACHE],
        key = "'find-music-simple-list-' + #keyword + '_' + #sort + '_' + #cursor?.toString()",
        sync = true
    )
    fun findMusicSimpleListByKeywordAndCondition(
        keyword: String,
        sort: MusicSort,
        cursor: MusicCursorDTO?,
        userId: Long
    ): List<MusicSimpleResponseDTO> {
        val musicDocuments = musicRepository
            .findMusicSimpleListByKeywordAndCondition(keyword, sort, cursor, userId)
        return musicDocuments.map { MusicSimpleResponseDTO(it) }
    }

    // TODO
    fun findMusicAndArtistListInAccuracyTop10() {}

    @Transactional(readOnly = true)
    @Cacheable(
        cacheNames = [FIVE_MIN_CACHE],
        key = "'find-music-details-' + #musicId",
        sync = true
    )
    fun findMusicDetailsByElasticsearch(musicId: Long, userId: Long): MusicDocumentResponseDTO {
        var musicDocument = findMusicDocumentById(musicId)
        musicDocument = musicRepository.addMusicDetailsByDocumentAndUserId(musicDocument, userId)
        val files = musicDocument.files.map { fileDocument ->
            FileDetailsDTO(fileDocument, musicDocument.id!!)
        }
        return MusicDocumentResponseDTO(musicDocument, files)
    }

    private fun findMusicDocumentById(musicId: Long) =
        musicSearchRepository.findById(musicId)
            .orElseThrow { MusicNotFoundException("Music not found with id: $musicId") }
}