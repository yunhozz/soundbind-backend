package com.music_service.domain.application

import com.music_service.domain.application.dto.request.MusicCreateDTO
import com.music_service.domain.application.dto.request.MusicUpdateDTO
import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import com.music_service.domain.application.dto.response.MusicFileResponseDTO
import com.music_service.domain.application.file.FileHandler
import com.music_service.domain.application.listener.AsyncListener
import com.music_service.domain.persistence.entity.FileEntity
import com.music_service.domain.persistence.entity.FileType
import com.music_service.domain.persistence.entity.FileType.IMAGE
import com.music_service.domain.persistence.entity.FileType.MUSIC
import com.music_service.domain.persistence.entity.Genre
import com.music_service.domain.persistence.entity.Music
import com.music_service.domain.persistence.entity.MusicLikes
import com.music_service.domain.persistence.repository.FileRepository
import com.music_service.domain.persistence.repository.MusicLikesRepository
import com.music_service.domain.persistence.repository.MusicRepository
import com.music_service.domain.persistence.repository.dto.MusicSimpleQueryDTO
import com.music_service.global.exception.MusicServiceException.MusicNotFoundException
import com.music_service.global.exception.MusicServiceException.MusicNotUpdatableException
import com.music_service.global.exception.MusicServiceException.NegativeValueException
import com.music_service.global.util.RedisUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class MusicService(
    private val musicRepository: MusicRepository,
    private val fileRepository: FileRepository,
    private val musicLikesRepository: MusicLikesRepository
) {

    @Autowired
    private lateinit var fileHandler: FileHandler

    @Autowired
    private lateinit var asyncListener: AsyncListener

    @Transactional
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
        musicRepository.save(music)

        val fileMap = hashMapOf<FileEntity, FileUploadResponseDTO>()

        val musicFileInfo = fileHandler.generateFileInfo(dto.musicFile)
        val musicFileEntity = createFileEntity(MUSIC, musicFileInfo, music)
        fileMap[musicFileEntity] = musicFileInfo

        dto.imageFile?.let {
            val imageFileInfo = fileHandler.generateFileInfo(it)
            val imageFileEntity = createFileEntity(IMAGE, imageFileInfo, music)
            fileMap[imageFileEntity] = imageFileInfo
        }

        asyncListener.onMusicUpload(fileMap, music)
        fileRepository.saveAll(fileMap.keys)

        return music.id!!
    }

    @Transactional(readOnly = true)
    fun findMusicsByKeyword(keyword: String, pageable: Pageable): Slice<MusicSimpleQueryDTO>
        = musicRepository.findMusicSimpleListByKeyword(keyword, pageable)

    @Transactional
    fun updateMusicInformation(id: Long, dto: MusicUpdateDTO): Long {
        val before30Days = LocalDateTime.now().minusDays(30)
        val music = musicRepository.findMusicEligibleForUpdateById(id, before30Days)
            ?: throw MusicNotUpdatableException("It can be modified after 30 days of final modification.")
        val fileEntities = fileRepository.findFilesWhereMusicId(music.id!!)

        var imageFileInfo: FileUploadResponseDTO? = null
        var fileUrl: String? = null

        dto.imageFile?.let { imageFile ->
            fileEntities.firstOrNull { fileEntity -> fileEntity.fileType == IMAGE }
                ?.let { imageFileEntity ->
                    fileUrl = imageFileEntity.fileUrl
                    fileHandler.generateFileInfo(imageFile).let { info ->
                        imageFileInfo = info
                        imageFileEntity.updateImage(
                            info.originalFileName,
                            info.savedName,
                            info.fileUrl
                        )
                    }
                }
        }
        music.updateInfo(
            dto.title,
            dto.genres.map { Genre.of(it) }.toSet()
        )
        asyncListener.onMusicUpdate(fileUrl, imageFileInfo, music, fileEntities)

        return music.id!!
    }

    @Transactional
    fun changeLikesFlag(musicId: Long, userId: Long): Long? {
        musicLikesRepository.findMusicLikesWithMusicByMusicId(musicId)?.let { ml ->
            try {
                ml.changeFlag()
                if (ml.flag) return ml.music.userId
            } catch (e: IllegalArgumentException) {
                throw NegativeValueException(e.localizedMessage)
            }
            return null
        } ?: run {
            val music = findMusicById(musicId).also { music ->
                val musicLikes = MusicLikes(music, userId)
                musicLikesRepository.save(musicLikes)
                music.addLikes(1)
            }
            return music.userId
        }
    }

    @Transactional
    fun downloadMusic(id: Long): MusicFileResponseDTO {
        val music = findMusicById(id)
        val files = fileRepository.findFilesWhereMusicId(music.id!!)

        files.firstOrNull { fileEntity -> fileEntity.fileType == MUSIC }
            ?.let { musicFileEntity ->
                val musicInfo = fileHandler.downloadMusic(musicFileEntity.fileUrl)
                return MusicFileResponseDTO(
                    musicInfo.resource,
                    musicInfo.contentType,
                    fileName = musicFileEntity.originalFileName
                )
            } ?: throw MusicNotFoundException("Music not found with id: $id")
    }

    @Transactional
    fun deleteMusic(id: Long) {
        val music = findMusicById(id)
        val files = fileRepository.findFilesWhereMusicId(music.id!!)

        asyncListener.onMusicDelete(music.id!!, files)
        fileRepository.deleteAllInBatch(files)
        music.softDelete()
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