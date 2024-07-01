package com.music_service.domain.persistence.repository

import com.music_service.domain.persistence.entity.QFileEntity.fileEntity
import com.music_service.domain.persistence.entity.QMusic.music
import com.music_service.global.dto.response.MusicDetailsQueryDTO
import com.music_service.global.dto.response.QMusicDetailsQueryDTO
import com.music_service.global.dto.response.QMusicDetailsQueryDTO_MusicFileQueryDTO
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class MusicQueryRepositoryImpl(
    private val queryFactory: JPAQueryFactory
): MusicQueryRepository {

    override fun findMusicDetailsById(id: Long): MusicDetailsQueryDTO? {
        val musicDetails = queryFactory
            .select(
                QMusicDetailsQueryDTO(
                    music.id,
                    music.userId,
                    music.userNickname,
                    music.title
                )
            )
            .from(music)
            .where(music.id.eq(id))
            .fetchOne()

//        val genres = queryFactory
//            .select(music.genres.any().stringValue())
//            .from(music)
//            .where(music.id.eq(id))
//            .fetch()

        return musicDetails?.let {
//            musicDetails.genres = genres
            val musicFiles = queryFactory
                .select(
                    QMusicDetailsQueryDTO_MusicFileQueryDTO(
                        fileEntity.id,
                        fileEntity.originalFileName,
                        fileEntity.savedName,
                        fileEntity.fileUrl
                    )
                )
                .from(fileEntity)
                .join(fileEntity.music, music)
                .where(music.id.eq(musicDetails.id))
                .fetch()

//            val fileEntityIds = musicFiles.map { it.id }
//            val fileTypes = queryFactory
//                .select(fileEntity.fileType.stringValue())
//                .from(fileEntity)
//                .where(fileEntity.id.`in`(fileEntityIds))
//                .fetch()

//            musicFiles.forEachIndexed { index, musicFileQueryDTO ->
//                musicFileQueryDTO.fileTypeName = fileTypes[index]
//            }
            musicDetails.files = musicFiles
            return musicDetails
        }
    }
}