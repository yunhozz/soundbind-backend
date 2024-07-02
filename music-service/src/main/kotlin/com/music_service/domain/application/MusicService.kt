package com.music_service.domain.application

import com.music_service.domain.application.file.FileHandler
import com.music_service.domain.interfaces.MusicServiceException.MusicNotFoundException
import com.music_service.domain.persistence.entity.FileEntity
import com.music_service.domain.persistence.entity.FileEntity.FileType.IMAGE
import com.music_service.domain.persistence.entity.FileEntity.FileType.MUSIC
import com.music_service.domain.persistence.entity.Music
import com.music_service.domain.persistence.repository.FileRepository
import com.music_service.domain.persistence.repository.MusicRepository
import com.music_service.global.dto.request.MusicCreateDTO
import com.music_service.global.dto.response.MusicDetailsQueryDTO
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.time.LocalDateTime

@Service
class MusicService(
    private val musicRepository: MusicRepository,
    private val fileRepository: FileRepository,
    private val fileHandler: FileHandler
) {
    // Spring rolls back only for RuntimeException, Error by default
    @Transactional(rollbackFor = [IOException::class])
    fun uploadMusic(dto: MusicCreateDTO): Long? {
        val genres: Set<Music.Genre> = dto.genres.map {
            Music.Genre.of(it)
        }.toHashSet()

        val music = Music(
            dto.userId,
            dto.title,
            dto.userNickname,
            genres
        )

        val musicFileInfo = fileHandler.uploadMusic(dto.musicFile)
        val musicFileEntity = createFileEntity(MUSIC, musicFileInfo, music)
        fileRepository.save(musicFileEntity)

        dto.imageFile?.let {
            val imageFileInfo = fileHandler.uploadImage(it)
            val imageFileEntity = createFileEntity(IMAGE, imageFileInfo, music)
            fileRepository.save(imageFileEntity)
        }

        musicRepository.save(music)
        return music.id
    }

    @Transactional(readOnly = true)
    fun findMusicDetails(id: Long): MusicDetailsQueryDTO {
        return musicRepository.findMusicDetailsById(id)
            ?: throw MusicNotFoundException("Music not found with id: $id")
    }

    // TODO
    @Transactional
    fun updateMusic(id: Long) {}

    // TODO
    @Transactional(readOnly = true)
    fun findMusics(): List<Music> = musicRepository.findAll()

    @Transactional
    fun deleteMusic(id: Long) {
        val music = musicRepository.findById(id)
            .orElseThrow { MusicNotFoundException("Music not found with id: $id") }
        music.id?.let {
            val files = fileRepository.findFilesWhereMusicId(it)
            files.forEach { file ->
                when(file.fileType) {
                    MUSIC -> fileHandler.deleteMusic(file.fileUrl)
                    IMAGE -> fileHandler.deleteImage(file.fileUrl)
                }
            }
            fileRepository.deleteAllInBatch(files)
            music.deletedAt = LocalDateTime.now()
        }
    }

    private fun createFileEntity(
        fileType: FileEntity.FileType,
        fileInfo: Triple<String, String, String>,
        music: Music,
    ): FileEntity
        = FileEntity(
            fileType,
            originalFileName = fileInfo.first,
            savedName = fileInfo.second,
            fileUrl = fileInfo.third
        ).apply { this.music = music }
}