package com.sound_bind.review_service.domain.persistence.repository

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders
import co.elastic.clients.elasticsearch.core.SearchRequest
import co.elastic.clients.json.JsonData
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sound_bind.review_service.domain.persistence.entity.QReview.review
import com.sound_bind.review_service.domain.persistence.entity.QReviewLikes.reviewLikes
import com.sound_bind.review_service.domain.persistence.es.ReviewDocument
import com.sound_bind.review_service.domain.persistence.repository.dto.QReviewLikesQueryDTO
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewCursorDTO
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewLikesQueryDTO
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewQueryDTO
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Repository

@Repository
class ReviewQueryRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val elasticsearchClient: ElasticsearchClient,
): ReviewQueryRepository {

    override fun findReviewsOnMusicByElasticsearch(
        musicId: Long,
        sort: ReviewSort,
        dto: ReviewCursorDTO?,
        pageable: Pageable
    ): MutableList<ReviewQueryDTO> {
        val boolQuery = QueryBuilders.bool()
            .must {
                it.term { t -> t.field("musicId").value(musicId) }
            }
        dto?.let {
            when (sort) {
                ReviewSort.LIKES ->
                    if (it.idCursor != null && it.likesCursor != null) {
                        boolQuery.must { s ->
                            s.range { r -> r.field("likes").lt(JsonData.of(it.likesCursor)) }
                        }
                    }
                ReviewSort.LATEST ->
                    if (it.idCursor != null && it.createdAtCursor != null) {
                        boolQuery.must { s ->
                            s.range { r -> r.field("createdAt").lt(JsonData.of(it.createdAtCursor)) }
                        }
                    }
            }
        }
        val searchRequest = SearchRequest.Builder()
            .index("review")
            .query { it.bool(boolQuery.build()) }
            .sort { s -> s.field { f -> f.field(sort.target).order(SortOrder.Desc) } }
            .sort { s -> s.field { f -> f.field("id").order(SortOrder.Desc) } }
            .size(pageable.pageSize + 1)
            .build()
        val searchResponse = elasticsearchClient.search(
            searchRequest,
            ReviewDocument::class.java
        )
        val metadata = searchResponse.hits()
        val reviews = metadata.hits().map { it.source() }

        return reviews.map { ReviewQueryDTO(it!!) }
            .toMutableList()
    }

    override fun processSliceQueryFromReviewIds(
        reviews: MutableList<ReviewQueryDTO>,
        userId: Long,
        pageable: Pageable
    ): Slice<ReviewQueryDTO> {
        val reviewIds = reviews.map { it.id }
        val reviewLikesList = queryFactory
            .select(
                QReviewLikesQueryDTO(
                    reviewLikes.id,
                    reviewLikes.userId,
                    review.id,
                    reviewLikes.flag
                )
            )
            .from(reviewLikes)
            .join(reviewLikes.review, review)
            .where(
                reviewLikes.userId.eq(userId),
                review.id.`in`(reviewIds)
            )
            .fetch()

        val reviewLikesListMap: Map<Long, List<ReviewLikesQueryDTO>> = reviewLikesList.groupBy { it.reviewId }
        reviews.forEach { review ->
            reviewLikesListMap[review.id]?.first()?.let { reviewLikesQueryDTO ->
                review.isLiked = reviewLikesQueryDTO.flag
            } ?: run { review.isLiked = false }
        }

        val pageSize = pageable.pageSize
        var hasNext = false
        if (reviews.size > pageSize) {
            reviews.removeAt(pageSize)
            hasNext = true
        }
        return SliceImpl(reviews, pageable, hasNext)
    }

    private fun reviewCursorLt(
        dto: ReviewCursorDTO?,
        sort: ReviewSort
    ): BooleanExpression? {
        dto?.let {
            when (sort) {
                ReviewSort.LIKES ->
                    if (it.idCursor != null && it.likesCursor != null) {
                        return review.likes.lt(it.likesCursor)
                            .or(review.likes.eq(it.likesCursor).and(review.id.lt(it.idCursor)))
                    }
                ReviewSort.LATEST ->
                    if (it.idCursor != null && it.createdAtCursor != null) {
                        return review.createdAt.lt(it.createdAtCursor)
                            .or(review.createdAt.eq(it.createdAtCursor).and(review.id.lt(it.idCursor)))
                    }
            }
        }
        return null
    }

    private fun sortReview(sort: ReviewSort): OrderSpecifier<*> =
        when (sort) {
            ReviewSort.LIKES -> review.likes.desc()
            ReviewSort.LATEST -> review.createdAt.desc()
        }
}