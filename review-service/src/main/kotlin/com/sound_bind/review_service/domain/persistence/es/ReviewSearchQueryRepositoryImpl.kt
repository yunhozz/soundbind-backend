package com.sound_bind.review_service.domain.persistence.es

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders
import co.elastic.clients.elasticsearch.core.SearchRequest
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewCursorDTO
import com.sound_bind.review_service.global.enums.ReviewSort
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class ReviewSearchQueryRepositoryImpl(private val es: ElasticsearchClient): ReviewSearchQueryRepository {

    override fun findReviewsOnMusicWithElasticsearch(
        musicId: Long,
        userId: Long,
        sort: ReviewSort,
        dto: ReviewCursorDTO?,
        pageable: Pageable
    ): List<ReviewDocument?> {
        val boolQuery = QueryBuilders.bool()
            .must {
                it.term { t -> t.field("musicId").value(musicId) }
            }
        val searchAfterValues = mutableListOf<FieldValue?>()

        dto?.let {
            when (sort) {
                ReviewSort.LIKES ->
                    if (it.idCursor != null && it.likesCursor != null) {
                        searchAfterValues.add(FieldValue.of(it.likesCursor))
                        searchAfterValues.add(FieldValue.of(it.idCursor))
                    }
                ReviewSort.LATEST ->
                    if (it.idCursor != null && it.createdAtCursor != null) {
                        searchAfterValues.add(FieldValue.of(it.createdAtCursor))
                        searchAfterValues.add(FieldValue.of(it.idCursor))
                    }
            }
        }

        val pitResponse = es.openPointInTime {
            it.index("review")
                .keepAlive { a -> a.time("1m") }
        }
        val pitId = pitResponse.id()

        val searchRequestBuilder = SearchRequest.Builder()
            .query { it.bool(boolQuery.build()) }
            .sort { s -> s.field { f -> f.field(sort.target).order(SortOrder.Desc) } }
            .sort { s -> s.field { f -> f.field("id").order(SortOrder.Desc) } }
            .pit { p -> p.id(pitId) }
        if (searchAfterValues.isNotEmpty()) {
            searchRequestBuilder.searchAfter(searchAfterValues)
        }

        val searchResponse = es.search(
            searchRequestBuilder.build(),
            ReviewDocument::class.java
        )
        val metadata = searchResponse.hits()
        val reviews = metadata.hits().map { it.source() }

        return reviews
    }
}