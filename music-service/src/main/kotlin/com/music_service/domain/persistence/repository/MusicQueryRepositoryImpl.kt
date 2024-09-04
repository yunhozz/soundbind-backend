package com.music_service.domain.persistence.repository

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders
import co.elastic.clients.elasticsearch.core.SearchRequest
import com.music_service.domain.persistence.entity.FileType
import com.music_service.domain.persistence.entity.QFileEntity.fileEntity
import com.music_service.domain.persistence.entity.QMusic.music
import com.music_service.domain.persistence.entity.QMusicLikes.musicLikes
import com.music_service.domain.persistence.es.document.MusicDocument
import com.music_service.domain.persistence.repository.dto.MusicCursorDTO
import com.music_service.domain.persistence.repository.dto.MusicLikesQueryDTO
import com.music_service.domain.persistence.repository.dto.MusicPartialQueryDTO
import com.music_service.domain.persistence.repository.dto.MusicSimpleQueryDTO
import com.music_service.domain.persistence.repository.dto.QMusicLikesQueryDTO
import com.music_service.domain.persistence.repository.dto.QMusicPartialQueryDTO
import com.music_service.domain.persistence.repository.dto.QMusicSimpleQueryDTO
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Repository

@Repository
class MusicQueryRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val elasticsearch: ElasticsearchClient
): MusicQueryRepository {

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
            .where(fileEntity.fileType.eq(FileType.IMAGE))
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

    override fun findMusicSimpleListByKeywordAndCondition(
        keyword: String,
        sort: MusicSort,
        cursor: MusicCursorDTO?,
        userId: Long
    ): List<MusicDocument?> {
        val boolQuery = QueryBuilders.bool()
            .should {
                it.match { m ->
                    m.field("title").query(keyword)
                }
            }
            .should {
                it.match { m ->
                    m.field("userNickname").query(keyword)
                }
            }
        val pit = elasticsearch.openPointInTime {
            it.index("music")
                .keepAlive { a -> a.time("1m") }
        }
        val searchRequestBuilder = SearchRequest.Builder()
            .query { it.bool(boolQuery.build()) }
            .sort { s -> s.field { f -> f.field("id").order(SortOrder.Desc) } }
            .pit { p -> p.id(pit.id()) }
            .size(20)
        if (sort == MusicSort.ACCURACY) {
            searchRequestBuilder.sort { s -> s.field { f -> f.field("_score").order(SortOrder.Desc) } }
        } else {
            searchRequestBuilder.sort { s -> s.field { f -> f.field(sort.target).order(SortOrder.Desc) } }
        }

        val searchAfterValues = mutableListOf<FieldValue?>()
        cursor?.let {
            when(sort) {
                MusicSort.ACCURACY -> searchAfterValues.add(FieldValue.of(it.accuracyCursor))
                MusicSort.LIKES -> searchAfterValues.add(FieldValue.of(it.likesCursor))
                MusicSort.LATEST -> searchAfterValues.add(FieldValue.of(it.createdAtCursor))
            }
        }
        if (searchAfterValues.isNotEmpty()) {
            searchRequestBuilder.searchAfter(searchAfterValues)
        }

        val musicDocumentList = elasticsearch.search(
            searchRequestBuilder.build(),
            MusicDocument::class.java
        ).hits().hits().map { it.source() }

        val musicIds = musicDocumentList.map { it?.id!! }
        val musicPartials = findMusicPartials(musicIds)
        val musicLikesList = findMusicLikesList(userId, musicIds)

        val musicPartialsMap = musicPartials.groupBy { it.id }
        val musicLikesListMap = musicLikesList.groupBy { it.musicId }

        musicDocumentList.forEach { music ->
            musicPartialsMap[music?.id]?.first()?.let { mp ->
                music?.updateLikesAndScoreAverage(mp.likes, mp.scoreAverage)
            }
            musicLikesListMap[music?.id]?.first()?.let { ml ->
                music?.updateIsLiked(ml.flag)
            } ?: run { music?.updateIsLiked(false) }
        }

        return musicDocumentList
    }

    override fun addMusicDetailsByDocumentAndUserId(musicDocument: MusicDocument, userId: Long): MusicDocument {
        val musicId = musicDocument.id!!
        val musicPartial = findMusicPartials(listOf(musicId)).first()
        musicDocument.updateLikesAndScoreAverage(musicPartial.likes, musicPartial.scoreAverage)

        val musicLikes = findMusicLikesList(userId, listOf(musicId))
        val isLiked = musicLikes.firstOrNull()?.flag ?: false
        musicDocument.updateIsLiked(isLiked)

        return musicDocument
    }

    private fun findMusicPartials(musicIds: List<Long>): List<MusicPartialQueryDTO> =
        queryFactory
            .select(
                QMusicPartialQueryDTO(
                    music.id,
                    music.likes,
                    music.scoreAverage
                )
            )
            .from(music)
            .where(music.id.`in`(musicIds))
            .fetch()

    private fun findMusicLikesList(userId: Long, musicIds: List<Long>): List<MusicLikesQueryDTO> =
        queryFactory
            .select(
                QMusicLikesQueryDTO(
                    musicLikes.id,
                    musicLikes.userId,
                    music.id,
                    musicLikes.flag
                )
            )
            .from(musicLikes)
            .join(musicLikes.music, music)
            .where(
                musicLikes.userId.eq(userId),
                music.id.`in`(musicIds)
            )
            .fetch()
}