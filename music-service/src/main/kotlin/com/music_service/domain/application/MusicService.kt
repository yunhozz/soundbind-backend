package com.music_service.domain.application

import com.music_service.global.handler.file.FileHandler
import com.music_service.domain.persistence.entity.Music
import com.music_service.domain.persistence.repository.MusicRepository
import com.music_service.global.dto.request.MusicCreateDTO
import com.music_service.global.dto.response.MusicDetailsDTO
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException

@Service
class MusicService(
    private val musicRepository: MusicRepository,
    private val fileHandler: FileHandler
) {
    // Spring rolls back only for RuntimeException, Error by default
    @Transactional(rollbackFor = [IOException::class])
    fun uploadMusic(dto: MusicCreateDTO): Long? {
        val genres: Set<Music.Genre> = dto.genres.map {
            Music.Genre.of(it)
        }.toHashSet()

        // Throws IOException
        fileHandler.uploadMusic(dto.musicFile)
        dto.imageFile?.let { fileHandler.uploadImage(it) }

        val music = Music(
            dto.userId,
            dto.title,
            dto.userNickname,
            genres,
            dto.musicUrl,
            dto.imageUrl
        )
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
}