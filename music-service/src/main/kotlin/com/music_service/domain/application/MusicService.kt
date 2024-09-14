package com.music_service.domain.application

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.music_service.domain.application.dto.request.MusicCreateDTO
import com.music_service.domain.application.dto.request.MusicUpdateDTO
import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import com.music_service.domain.application.dto.response.MusicDetailsDTO
import com.music_service.domain.application.dto.response.MusicFileResponseDTO
import com.music_service.domain.application.manager.ElasticsearchManager
import com.music_service.domain.application.manager.FileManager
import com.music_service.domain.application.manager.LockManager
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
import com.music_service.global.exception.MusicServiceException.MusicNotFoundException
import com.music_service.global.exception.MusicServiceException.MusicNotUpdatableException
import com.music_service.global.exception.MusicServiceException.NegativeValueException
import com.music_service.global.util.RedisUtils
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class MusicService(
    private val musicRepository: MusicRepository,
    private val fileRepository: FileRepository,
    private val musicLikesRepository: MusicLikesRepository,
    private val fileManager: FileManager,
    private val elasticsearchManager: ElasticsearchManager,
    private val lockManager: LockManager
) {

    @Transactional
    fun uploadMusic(userId: Long, dto: MusicCreateDTO): Long {
        val userInfo = RedisUtils.getJson("user:$userId", Map::class.java)
        val music = Music.create(
            userId,
            userInfo["nickname"] as String,
            dto.title,
            dto.genres.map { Genre.of(it) }.toSet()
        )
        musicRepository.save(music)

        val fileInfoList = arrayListOf<FileUploadResponseDTO>()
        val fileEntities = arrayListOf<FileEntity>()

        val musicFileInfo = fileManager.generateFileInfo(dto.musicFile)
        val musicFileEntity = createFileEntity(MUSIC, musicFileInfo, music)
        fileInfoList.add(musicFileInfo)
        fileEntities.add(musicFileEntity)

        dto.imageFile?.let {
            val imageFileInfo = fileManager.generateFileInfo(it)
            val imageFileEntity = createFileEntity(IMAGE, imageFileInfo, music)
            fileInfoList.add(imageFileInfo)
            fileEntities.add(imageFileEntity)
        }

        fileInfoList.forEach { fileManager.onMusicUpload(it) }
        fileRepository.saveAll(fileEntities)
        elasticsearchManager.onMusicUpload(MusicDetailsDTO(music, fileEntities))

        return music.id!!
    }

    @Transactional
    fun updateMusicInformation(id: Long, dto: MusicUpdateDTO): Long {
        val before30Days = LocalDateTime.now().minusDays(30)
        val music = musicRepository.findMusicEligibleForUpdateById(id, before30Days)
            ?: throw MusicNotUpdatableException("It can be modified after 30 days of final modification.")
        val fileEntities = fileRepository.findFilesWhereMusicId(music.id!!)

        dto.imageFile?.let { imageFile ->
            fileEntities.firstOrNull { fileEntity -> fileEntity.fileType == IMAGE }
                ?.let { imageFileEntity ->
                    val fileUrl = imageFileEntity.fileUrl
                    fileManager.generateFileInfo(imageFile).let { info ->
                        imageFileEntity.updateImage(
                            info.originalFileName,
                            info.savedName,
                            info.fileUrl
                        )
                        fileManager.onMusicUpdate(fileUrl, info)
                    }
                }
        }
        music.updateInfo(
            dto.title,
            dto.genres.map { Genre.of(it) }.toSet()
        )
        elasticsearchManager.onMusicUpload(MusicDetailsDTO(music, fileEntities))

        return music.id!!
    }

    @Transactional
    fun changeLikesFlag(musicId: Long, userId: Long): Long? {
        musicLikesRepository.findMusicLikesWithMusicByMusicIdAndUserId(musicId, userId)?.let { ml ->
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
    @KafkaListener(groupId = "music-service-group", topics = ["review-score-topic"])
    fun changeMusicScoreAverageByReviewTopic(@Payload payload: String) {
        val obj = mapper.readValue(payload, Map::class.java)
        val musicId = obj["musicId"] as Number
        val score = obj["score"] as Double
        val oldScore = obj["oldScore"] as Double?

        val music = findMusicById(musicId.toLong())
        oldScore?.let {
            music.updateScoreByReviewUpdate(it, score)
        } ?: run {
            when {
                score > 0 -> music.updateScoreByReviewAdd(score)
                score < 0 -> music.updateScoreByReviewRemove(score)
            }
        }
    }

    @Transactional
    fun downloadMusic(id: Long): MusicFileResponseDTO {
        val music = findMusicById(id)
        val files = fileRepository.findFilesWhereMusicId(music.id!!)

        files.firstOrNull { fileEntity -> fileEntity.fileType == MUSIC }
            ?.let { musicFileEntity ->
                val musicInfo = fileManager.downloadMusic(musicFileEntity.fileUrl)
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

        files
            .map { it.fileUrl }
            .forEach { fileManager.onMusicDelete(it) }
        elasticsearchManager
            .onMusicDelete(music.id!!, files.map { it.id!! })

        fileRepository.deleteAllInBatch(files)
        music.softDelete()
    }

    @Transactional
    @KafkaListener(groupId = "music-service-group", topics = ["user-deletion-topic"])
    fun deleteMusicsByUserWithdraw(@Payload payload: String) {
        val obj = mapper.readValue(payload, Map::class.java)
        val userId = obj["userId"] as Number

        val musics = musicRepository.findMusicByUserId(userId.toLong())
        val musicLikesList = musicLikesRepository.findMusicLikesByUserId(userId.toLong())
        musicLikesRepository.deleteAllInBatch(musicLikesList)

        val fileUrls = arrayListOf<String>()
        musics.forEach { music ->
            val files = fileRepository.findFilesWhereMusicId(music.id!!)
            fileUrls.addAll(files.map { it.fileUrl })

            elasticsearchManager.onMusicDelete(music.id!!, files.map { it.id!! })
            music.softDelete()
            fileRepository.deleteAllInBatch(files)
        }
        fileUrls.forEach { fileManager.onMusicDelete(it) }
    }

    @Transactional(readOnly = true)
    fun findMusicianIdByMusicId(musicId: Long): Long =
        musicRepository.findMusicianIdById(musicId)
            ?: throw MusicNotFoundException("Music not found with id: $musicId")

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

    companion object {
        private val mapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
}