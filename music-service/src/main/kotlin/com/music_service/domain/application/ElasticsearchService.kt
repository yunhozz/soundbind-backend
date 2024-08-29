package com.music_service.domain.application

import com.music_service.domain.application.dto.response.FileDetailsDTO
import com.music_service.domain.application.dto.response.MusicDetailsDTO
import com.music_service.domain.persistence.es.FileDocument
import com.music_service.domain.persistence.es.FileSearchRepository
import com.music_service.domain.persistence.es.MusicDocument
import com.music_service.domain.persistence.es.MusicSearchRepository
import com.music_service.global.exception.MusicServiceException.MusicNotFoundException
import org.springframework.stereotype.Service

@Service
class ElasticsearchService(
    private val musicSearchRepository: MusicSearchRepository,
    private val fileSearchRepository: FileSearchRepository
) {

    fun indexMusicByElasticsearch(dto: MusicDetailsDTO) {
        val fileDocumentList = dto.files.map {
            FileDocument(
                it.id,
                it.musicId,
                it.fileType,
                it.originalFileName,
                it.savedName,
                it.fileUrl
            )
        }
        val musicDocument = MusicDocument(
            dto.id,
            dto.userId,
            dto.userNickname,
            dto.title,
            dto.genres,
            fileDocumentList
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