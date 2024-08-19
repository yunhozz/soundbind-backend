package com.music_service.domain.persistence.repository

import com.music_service.domain.persistence.entity.FileEntity.FileType.IMAGE
import com.music_service.domain.persistence.entity.QFileEntity.fileEntity
import com.music_service.domain.persistence.entity.QMusic.music
import com.music_service.domain.persistence.repository.dto.MusicDetailsQueryDTO
import com.music_service.domain.persistence.repository.dto.MusicSimpleQueryDTO
import com.music_service.domain.persistence.repository.dto.QMusicFileQueryDTO
import com.music_service.domain.persistence.repository.dto.QMusicSimpleQueryDTO
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Repository

@Repository
class MusicQueryRepositoryImpl(
    private val queryFactory: JPAQueryFactory
): MusicQueryRepository {

    override fun findMusicDetailsById(id: Long): MusicDetailsQueryDTO? {
        val m = queryFactory
            .selectFrom(music)
            .where(music.id.eq(id))
            .fetchOne()

        return m?.let {
            val musicFiles = queryFactory
                .select(
                    QMusicFileQueryDTO(
                        fileEntity.id,
                        fileEntity.originalFileName,
                        fileEntity.savedName,
                        fileEntity.fileUrl
                    )
                )
                .from(fileEntity)
                .join(fileEntity.music, music)
                .where(music.id.eq(m.id))
                .fetch()

            val musicDetailsQueryDTO = MusicDetailsQueryDTO(
                m.id!!,
                m.userId,
                m.userNickname,
                m.title,
                m.genres,
                m.createdAt,
                m.updatedAt
            )
            musicDetailsQueryDTO.files = musicFiles
            return musicDetailsQueryDTO
        }
    }

    override fun findMusicSimpleListByKeyword(keyword: String, pageable: Pageable): Slice<MusicSimpleQueryDTO> {
        val pageSize = pageable.pageSize
        val musics = queryFactory
            .select(
                QMusicSimpleQueryDTO(
                    music.id,
                    music.userNickname,
                    music.title,
                    fileEntity.fileUrl
                )
            )
            .from(fileEntity)
            .join(fileEntity.music, music)
            .where(
                music.userNickname.contains(keyword)
                    .or(music.title.contains(keyword))
            )
            .where(fileEntity.fileType.eq(IMAGE))
            .orderBy(music.id.desc())
            .limit(pageSize.toLong() + 1)
            .limit(100)
            .fetch()

        var hasNext = false
        if (musics.size > pageSize) {
            musics.removeAt(pageSize)
            hasNext = true
        }

        return SliceImpl(musics, pageable, hasNext)
    }
}