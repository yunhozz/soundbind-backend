package com.music_service.domain.application

import com.music_service.domain.application.dto.message.MusicReviewMessageDTO
import com.music_service.domain.application.dto.message.UserWithdrawMessageDTO
import com.music_service.domain.application.dto.request.MusicCreateDTO
import com.music_service.domain.application.dto.request.MusicUpdateDTO
import com.music_service.domain.application.dto.response.FileUploadResponseDTO
import com.music_service.domain.application.dto.response.MusicDetailsDTO
import com.music_service.domain.application.dto.response.MusicFileResponseDTO
import com.music_service.domain.application.manager.AsyncManager
import com.music_service.domain.application.manager.CacheManager
import com.music_service.domain.application.manager.FileManager
import com.music_service.domain.application.manager.KafkaManager
import com.music_service.domain.application.manager.LockManager
import com.music_service.domain.persistence.entity.FileEntity
import com.music_service.domain.persistence.entity.FileType
import com.music_service.domain.persistence.entity.FileType.IMAGE
import com.music_service.domain.persistence.entity.FileType.MUSIC
import com.music_service.domain.persistence.entity.Genre
import com.music_service.domain.persistence.entity.Music
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
    private val asyncManager: AsyncManager,
    private val fileManager: FileManager,
    private val kafkaManager: KafkaManager,
    private val lockManager: LockManager,
    private val cacheManager: CacheManager
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

        fileInfoList.forEach { asyncManager.musicUploadWithAsync(it) }
        fileRepository.saveAll(fileEntities)
        asyncManager.saveMusicByElasticsearchWithAsync(MusicDetailsDTO(music, fileEntities))
        clearMusicCacheByUpdate(music.id!!)

        return music.id!!
    }

    @Transactional
    fun updateMusicInformation(musicId: Long, dto: MusicUpdateDTO): Long {
        val before30Days = LocalDateTime.now().minusDays(30)
        val music = musicRepository.findMusicEligibleForUpdateById(musicId, before30Days)
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
                        asyncManager.musicUpdateWithAsync(fileUrl, info)
                    }
                }
        }
        music.updateInfo(
            dto.title,
            dto.genres.map { Genre.of(it) }.toSet()
        )
        asyncManager.saveMusicByElasticsearchWithAsync(MusicDetailsDTO(music, fileEntities))
        clearMusicCacheByUpdate(music.id!!)

        return music.id!!
    }

    @Transactional
    fun changeLikesFlag(musicId: Long, userId: Long) {
        try {
            lockManager.changeLikesFlagWithLock(musicId, userId)?.let {
                val myInfo = RedisUtils.getJson("user:$userId", Map::class.java)
                kafkaManager.sendMusicLikeTopic(it,
                    "${myInfo["nickname"] as String} 님이 당신의 음원에 좋아요를 눌렀습니다.")
            }
            clearMusicCacheByUpdate(musicId)

        } catch (e: IllegalArgumentException) {
            throw NegativeValueException(e.localizedMessage)
        }
    }

    @Transactional
    @KafkaListener(groupId = "music-service-group", topics = ["music-review-topic"])
    fun changeScoreAverageAndSendNotification(@Payload payload: MusicReviewMessageDTO) {
        val music = findMusicById(payload.musicId)
        val score = payload.score

        payload.oldScore?.let {
            music.updateScoreByReviewUpdate(it, score)
        } ?: run {
            when {
                score > 0 -> music.updateScoreByReviewAdd(score)
                score < 0 -> music.updateScoreByReviewRemove(score)
            }
        }
        clearMusicCacheByUpdate(music.id!!)

        val reviewId = payload.reviewId
        val nickname = payload.nickname

        if (reviewId != null && nickname != null) {
            kafkaManager.sendReviewAddedTopic(
                music.userId,
                content = "$nickname 님이 당신의 음원에 리뷰를 남겼습니다.",
                link = "http://localhost:8000/api/reviews/$reviewId"
            )
        }
    }

    @Transactional
    fun downloadMusic(musicId: Long): MusicFileResponseDTO {
        val music = findMusicById(musicId)
        val files = fileRepository.findFilesWhereMusicId(music.id!!)

        files.firstOrNull { fileEntity -> fileEntity.fileType == MUSIC }
            ?.let { musicFileEntity ->
                val musicInfo = fileManager.downloadMusic(musicFileEntity.fileUrl)
                return MusicFileResponseDTO(
                    musicInfo.resource,
                    musicInfo.contentType,
                    fileName = musicFileEntity.originalFileName
                )
            } ?: throw MusicNotFoundException("Music not found with id: $musicId")
    }

    @Transactional
    fun deleteMusic(musicId: Long) {
        val music = findMusicById(musicId)
        val files = fileRepository.findFilesWhereMusicId(music.id!!)

        files.map { it.fileUrl }
            .forEach { asyncManager.musicDeleteWithAsync(it) }
        asyncManager.deleteMusicByElasticsearchWithAsync(music.id!!, files.map { it.id!! })

        fileRepository.deleteAllInBatch(files)
        music.softDelete()
        clearMusicCacheByUpdate(music.id!!)
    }

    @Transactional
    @KafkaListener(groupId = "music-service-group", topics = ["user-deletion-topic"])
    fun deleteMusicsByUserWithdraw(@Payload payload: UserWithdrawMessageDTO) {
        val userId = payload.userId
        val musics = musicRepository.findMusicByUserId(userId)
        val musicLikesList = musicLikesRepository.findMusicLikesByUserId(userId)

        musicLikesRepository.deleteAllInBatch(musicLikesList)

        val fileUrls = arrayListOf<String>()
        musics.forEach { music ->
            val files = fileRepository.findFilesWhereMusicId(music.id!!)
            fileUrls.addAll(files.map { it.fileUrl })

            asyncManager.deleteMusicByElasticsearchWithAsync(music.id!!, files.map { it.id!! })
            music.softDelete()
            fileRepository.deleteAllInBatch(files)
            clearMusicCacheByUpdate(music.id!!)
        }
        fileUrls.forEach { asyncManager.musicDeleteWithAsync(it) }
    }

    private fun findMusicById(id: Long): Music =
        musicRepository.findById(id)
            .orElseThrow { MusicNotFoundException("Music not found with id: $id") }

    private fun clearMusicCacheByUpdate(musicId: Long) =
        cacheManager.run {
            clearMusicSimpleSearchResultsCache()
            clearMusicDetailsCache(musicId)
        }

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