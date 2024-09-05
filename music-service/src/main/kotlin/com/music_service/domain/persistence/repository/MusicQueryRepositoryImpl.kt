package com.music_service.domain.persistence.repository

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScore
import co.elastic.clients.elasticsearch._types.query_dsl.Query
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.util.ObjectBuilder
import com.music_service.domain.persistence.entity.QMusic.music
import com.music_service.domain.persistence.entity.QMusicLikes.musicLikes
import com.music_service.domain.persistence.es.document.MusicDocument
import com.music_service.domain.persistence.repository.dto.MusicCursorDTO
import com.music_service.domain.persistence.repository.dto.MusicLikesQueryDTO
import com.music_service.domain.persistence.repository.dto.MusicPartialQueryDTO
import com.music_service.domain.persistence.repository.dto.QMusicLikesQueryDTO
import com.music_service.domain.persistence.repository.dto.QMusicPartialQueryDTO
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class MusicQueryRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val elasticsearch: ElasticsearchClient
): MusicQueryRepository {

    override fun findMusicSimpleListByKeywordAndCondition(
        keyword: String,
        sort: MusicSort,
        cursor: MusicCursorDTO?,
        userId: Long
    ): List<MusicDocument?> {
        val searchRequestBuilder = SearchRequest.Builder()
            .pit { p ->
                val pit = elasticsearch.openPointInTime {
                    it.index("music")
                        .keepAlive { a -> a.time("1m") }
                }
                p.id(pit.id())
            }
            .size(20)
        if (sort == MusicSort.ACCURACY) {
            searchRequestBuilder.query { q ->
                q.functionScore { fs ->
                    fs.query { q -> createWildcardWithKeyword(q, keyword) }
                    fs.functions { f -> createScriptScore(f) }
                }
            }
        } else {
            searchRequestBuilder
                .query { q -> createWildcardWithKeyword(q, keyword) }
                .sort { s ->
                    s.field { f -> f.field(sort.target).order(SortOrder.Desc) }
                }
        }

        val searchAfterValues = arrayListOf<FieldValue?>()
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

    private fun createWildcardWithKeyword(qb: Query.Builder, keyword: String): ObjectBuilder<Query>? =
        qb.bool { b ->
            b.should { s ->
                s.wildcard { w -> w.field("title").value("*$keyword*") }
            }
            b.should { s ->
                s.wildcard { w -> w.field("userNickname").value("*$keyword*") }
            }
        }

    private fun createScriptScore(fb: FunctionScore.Builder): FunctionScore.Builder.ContainerBuilder? {
        val script = """
            Math.log(2 + (doc['title.keyword'].size() > 0 ? doc['title.keyword'].value.length() : 0))
             + Math.log(2 + (doc['userNickname.keyword'].size() > 0 ? doc['userNickname.keyword'].value.length() : 0))
        """.trimIndent()
        return fb.scriptScore { ss ->
            ss.script { s ->
                s.inline { i -> i.source(script) }
            }
        }
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