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

        return musicDetails?.let {
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

            musicDetails.files = musicFiles
            return musicDetails
        }
    }
}