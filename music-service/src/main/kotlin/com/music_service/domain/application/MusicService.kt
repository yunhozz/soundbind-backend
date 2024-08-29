package com.music_service.domain.application

import com.music_service.domain.application.dto.request.MusicCreateDTO
import com.music_service.domain.application.dto.request.MusicUpdateDTO
import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import com.music_service.domain.application.dto.response.MusicDetailsDTO
import com.music_service.domain.application.dto.response.MusicFileResponseDTO
import com.music_service.domain.application.file.ImageHandler
import com.music_service.domain.application.file.MusicHandler
import com.music_service.domain.application.listener.ElasticsearchListener
import com.music_service.domain.persistence.entity.FileEntity
import com.music_service.domain.persistence.entity.FileType
import com.music_service.domain.persistence.entity.FileType.IMAGE
import com.music_service.domain.persistence.entity.FileType.MUSIC
import com.music_service.domain.persistence.entity.Genre
import com.music_service.domain.persistence.entity.Music
import com.music_service.domain.persistence.repository.FileRepository
import com.music_service.domain.persistence.repository.MusicRepository
import com.music_service.domain.persistence.repository.dto.MusicDetailsQueryDTO
import com.music_service.domain.persistence.repository.dto.MusicSimpleQueryDTO
import com.music_service.global.exception.MusicServiceException.MusicNotFoundException
import com.music_service.global.util.RedisUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException

@Service
class MusicService(
    private val musicRepository: MusicRepository,
    private val fileRepository: FileRepository,
    private val musicHandler: MusicHandler,
    private val imageHandler: ImageHandler
) {

    @Autowired
    private lateinit var elasticsearchListener: ElasticsearchListener

    // Spring rolls back only for RuntimeException, Error by default
    @Transactional(rollbackFor = [IOException::class])
    fun uploadMusic(userId: Long, dto: MusicCreateDTO): Long {
        val genres = dto.genres.map { Genre.of(it) }.toSet()
        val userInfo = RedisUtils.getJson("user:$userId", Map::class.java)
            ?: throw IllegalArgumentException("Value is not Present by Key : user:$userId")
        val music = Music.create(
            userId,
            userInfo["nickname"] as String,
            dto.title,
            genres
        )

        val files = arrayListOf<FileEntity>()

        val musicFileInfo = musicHandler.uploadMusic(dto.musicFile)
        val musicFileEntity = createFileEntity(MUSIC, musicFileInfo, music)
        fileRepository.save(musicFileEntity)
        files.add(musicFileEntity)

        dto.imageFile?.let {
            val imageFileInfo = imageHandler.uploadImage(it)
            val imageFileEntity = createFileEntity(IMAGE, imageFileInfo, music)
            fileRepository.save(imageFileEntity)
            files.add(imageFileEntity)
        }

        musicRepository.save(music)
        elasticsearchListener.onMusicUpload(MusicDetailsDTO(music, files))

        return music.id!!
    }

    @Transactional(readOnly = true)
    fun findMusicDetails(id: Long): MusicDetailsQueryDTO = musicRepository.findMusicDetailsById(id)
        ?: throw MusicNotFoundException("Music not found with id: $id")

    @Transactional(readOnly = true)
    fun findMusicsByKeyword(keyword: String, pageable: Pageable): Slice<MusicSimpleQueryDTO>
        = musicRepository.findMusicSimpleListByKeyword(keyword, pageable)

    @Transactional
    fun updateMusic(id: Long, dto: MusicUpdateDTO): Long {
        val music = findMusicById(id)
        val files = fileRepository.findFilesWhereMusicId(music.id!!)

        files.forEach { file ->
            if (file.fileType == IMAGE) {
                imageHandler.updateImage(file.fileUrl, dto.imageFile)
                fileRepository.save(file)
            }
        }
        music.updateInfo(
            dto.title,
            dto.genres.map { Genre.of(it) }.toSet()
        )
        elasticsearchListener.onMusicUpload(MusicDetailsDTO(music, files))

        return music.id!!
    }

    @Transactional
    fun downloadMusic(id: Long): MusicFileResponseDTO {
        val music = findMusicById(id)
        val files = fileRepository.findFilesWhereMusicId(music.id!!)
        files.forEach { file ->
            if (file.fileType == MUSIC) {
                val musicInfo = musicHandler.downloadMusic(file.fileUrl)
                musicHandler.downloadMusic(file.fileUrl)
                return MusicFileResponseDTO(
                    musicInfo.resource,
                    musicInfo.contentType,
                    fileName = file.originalFileName
                )
            }
        }
        throw MusicNotFoundException("Music not found with id: $id")
    }

    @Transactional
    fun deleteMusic(id: Long) {
        val music = findMusicById(id)
        val files = fileRepository.findFilesWhereMusicId(music.id!!)

        val fileIds = files.map { file ->
            when (file.fileType) {
                MUSIC -> musicHandler.deleteMusic(file.fileUrl)
                IMAGE -> imageHandler.deleteImage(file.fileUrl)
            }
            file.id!!
        }
        fileRepository.deleteAllInBatch(files)
        music.softDelete()
        elasticsearchListener.onMusicDelete(music.id!!, fileIds)
    }

    private fun findMusicById(id: Long): Music =
        musicRepository.findById(id)
            .orElseThrow { MusicNotFoundException("Music not found with id: $id") }

    private fun createFileEntity(
        fileType: FileType,
        fileInfo: FileUploadResponseDTO,
        music: Music
    ) = FileEntity.create(
            fileType,
            fileInfo.originalFileName,
            fileInfo.savedName,
            fileInfo.fileUrl,
            music
        )
}