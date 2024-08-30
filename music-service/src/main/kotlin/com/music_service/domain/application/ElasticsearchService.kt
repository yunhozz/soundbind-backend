package com.music_service.domain.application

import com.music_service.domain.application.dto.response.FileDetailsDTO
import com.music_service.domain.application.dto.response.MusicDetailsDTO
import com.music_service.domain.persistence.es.document.FileDocument
import com.music_service.domain.persistence.es.document.MusicDocument
import com.music_service.domain.persistence.es.search.FileSearchRepository
import com.music_service.domain.persistence.es.search.MusicSearchRepository
import com.music_service.global.exception.MusicServiceException.MusicNotFoundException
import com.music_service.global.util.DateTimeUtils
import org.springframework.stereotype.Service

@Service
class ElasticsearchService(
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
            fileDocumentList,
            DateTimeUtils.convertLocalDateTimeToString(dto.createdAt),
            DateTimeUtils.convertLocalDateTimeToString(dto.updatedAt)
        )
        fileSearchRepository.saveAll(fileDocumentList)
        musicSearchRepository.save(musicDocument)
    }

    fun findMusicDetailsByElasticsearch(musicId: Long): MusicDetailsDTO {
        val musicDocument = musicSearchRepository.findById(musicId)
            .orElseThrow { MusicNotFoundException("Music not found with id: $musicId") }
        val files = musicDocument.files.map { fileDocument ->
            FileDetailsDTO(fileDocument, musicDocument.id!!)
        }
        return MusicDetailsDTO(musicDocument, files)
    }

    fun deleteMusicByElasticsearch(musicId: Long, fileIds: List<Long>) {
        musicSearchRepository.deleteById(musicId)
        fileSearchRepository.deleteAllById(fileIds)
    }
}