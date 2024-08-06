package com.music_service.domain.application

import com.music_service.domain.application.dto.request.MusicCreateDTO
import com.music_service.domain.application.dto.request.MusicUpdateDTO
import com.music_service.domain.application.dto.response.MusicFileResponseDTO
import com.music_service.domain.application.file.FileHandler
import com.music_service.domain.persistence.entity.FileEntity
import com.music_service.domain.persistence.entity.FileEntity.FileType.IMAGE
import com.music_service.domain.persistence.entity.FileEntity.FileType.MUSIC
import com.music_service.domain.persistence.entity.Music
import com.music_service.domain.persistence.repository.FileRepository
import com.music_service.domain.persistence.repository.MusicRepository
import com.music_service.domain.persistence.repository.dto.MusicDetailsQueryDTO
import com.music_service.domain.persistence.repository.dto.MusicSimpleQueryDTO
import com.music_service.global.exception.MusicServiceException.MusicFileNotExistException
import com.music_service.global.exception.MusicServiceException.MusicNotFoundException
import com.music_service.global.util.RedisUtils
import org.springframework.core.io.Resource
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
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
    fun uploadMusic(userId: Long, dto: MusicCreateDTO): Long? {
        val genres: Set<Music.Genre> = dto.genres.map {
            Music.Genre.of(it)
        }.toHashSet()
        val userInfo = RedisUtils.getJson("user:$userId", Map::class.java)
            ?: throw IllegalArgumentException("Value is not Present by Key : user:$userId")
        val music = Music.create(
            userId,
            userInfo["nickname"] as String,
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
    fun findMusicDetails(id: Long): MusicDetailsQueryDTO = musicRepository.findMusicDetailsById(id)
        ?: throw MusicNotFoundException("Music not found with id: $id")

    @Transactional(readOnly = true)
    fun findMusicsByKeyword(keyword: String, pageable: Pageable): Slice<MusicSimpleQueryDTO>
        = musicRepository.findMusicSimpleListByKeyword(keyword, pageable)

    @Transactional
    fun updateMusic(id: Long, dto: MusicUpdateDTO): Long? {
        val music = findMusicById(id)
        music.id?.let {
            val files = fileRepository.findFilesWhereMusicId(it)
            files.forEach { file ->
                if (file.fileType == IMAGE) {
                    fileHandler.updateImage(file.fileUrl, dto.imageFile)
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

    @Transactional
    fun downloadMusic(id: Long): MusicFileResponseDTO {
        val music = findMusicById(id)
        return music.id?.let {
            val files = fileRepository.findFilesWhereMusicId(it)
            var musicInfo: Pair<Resource, String>?
            files.forEach { file ->
                if (file.fileType == MUSIC) {
                    musicInfo = fileHandler.downloadMusic(file.fileUrl)
                    return musicInfo?.let { info ->
                        MusicFileResponseDTO(
                            musicFile = info.first,
                            contentType = info.second,
                            fileName = file.originalFileName
                        )
                    } ?: throw MusicFileNotExistException("Music file download failed for id: $id")
                }
            }
            throw MusicFileNotExistException("Music file not found for id: $id")
        } ?: throw MusicNotFoundException("Music not found with id: $id")
    }

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
        = FileEntity.create(
            fileType,
            originalFileName = fileInfo.first,
            savedName = fileInfo.second,
            fileUrl = fileInfo.third,
            music
        )
}