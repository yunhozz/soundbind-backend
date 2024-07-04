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
import com.music_service.global.dto.request.MusicUpdateDTO
import com.music_service.global.dto.response.MusicDetailsQueryDTO
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException

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
            dto.userNickname,
            dto.title,
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

    @Transactional
    fun updateMusic(id: Long, dto: MusicUpdateDTO): Long? {
        val music = findMusicById(id)
        music.id?.let {
            val files = fileRepository.findFilesWhereMusicId(it)
            files.forEach { file ->
                if (file.fileType == IMAGE) {
                    fileHandler.deleteImage(file.fileUrl)
                    fileHandler.uploadImage(dto.imageFile)
                    fileRepository.save(file)
                }
            }
        }
        music.updateInfo(
            dto.title,
            dto.genres.map { Music.Genre.of(it) }
                .toMutableSet()
        )
        return music.id
    }

    // TODO
    @Transactional(readOnly = true)
    fun findMusics(): List<Music> = musicRepository.findAll()

    @Transactional
    fun deleteMusic(id: Long) {
        val music = findMusicById(id)
        music.id?.let {
            val files = fileRepository.findFilesWhereMusicId(it)
            files.forEach { file ->
                when(file.fileType) {
                    MUSIC -> fileHandler.deleteMusic(file.fileUrl)
                    IMAGE -> fileHandler.deleteImage(file.fileUrl)
                }
            }
            fileRepository.deleteAllInBatch(files)
            music.softDelete()
        }
    }

    private fun findMusicById(id: Long): Music {
        return musicRepository.findById(id)
            .orElseThrow { MusicNotFoundException("Music not found with id: $id") }
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