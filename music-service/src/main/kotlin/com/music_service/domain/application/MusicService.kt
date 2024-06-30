package com.music_service.domain.application

import com.music_service.domain.persistence.entity.FileEntity
import com.music_service.domain.persistence.entity.Music
import com.music_service.domain.persistence.repository.FileRepository
import com.music_service.domain.persistence.repository.MusicRepository
import com.music_service.global.dto.request.MusicCreateDTO
import com.music_service.global.dto.response.MusicDetailsDTO
import com.music_service.global.handler.file.FileHandler
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
            dto.title,
            dto.userNickname,
            genres
        )

        val musicFileInfo = fileHandler.uploadMusic(dto.musicFile)
        val musicFileEntity = createFileEntity(FileEntity.FileType.MUSIC, musicFileInfo, music)
        fileRepository.save(musicFileEntity)

        dto.imageFile?.let {
            val imageFileInfo = fileHandler.uploadImage(it)
            val imageFileEntity = createFileEntity(FileEntity.FileType.IMAGE, imageFileInfo, music)
            fileRepository.save(imageFileEntity)
        }

        musicRepository.save(music)
        return music.id
    }

    @Transactional(readOnly = true)
    fun findMusicDetails(id: Long): MusicDetailsDTO {
        val music = musicRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Music does not exist") }
        val genres: List<String> = music.genres.map { it.genreName }

        return MusicDetailsDTO(
            music.id,
            music.userId,
            music.userNickname,
            music.title,
            genres,
            music.musicUrl,
            music.imageUrl
        )
    }

    // TODO
    @Transactional
    fun updateMusic(id: Long) {}

    // TODO
    @Transactional(readOnly = true)
    fun findMusics(): List<Music> = musicRepository.findAll()

    // TODO
    @Transactional
    fun deleteMusic(id: Long) = musicRepository.deleteById(id)

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