package com.sound_bind.review_service.domain.persistence.repository

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders
import co.elastic.clients.elasticsearch.core.SearchRequest
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sound_bind.review_service.domain.persistence.entity.QReview.review
import com.sound_bind.review_service.domain.persistence.entity.QReviewLikes.reviewLikes
import com.sound_bind.review_service.domain.persistence.es.ReviewDocument
import com.sound_bind.review_service.domain.persistence.repository.dto.QReviewLikesQueryDTO
import com.sound_bind.review_service.domain.persistence.repository.dto.QReviewQueryDTO
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewCursorDTO
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewLikesQueryDTO
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewQueryDTO
import com.sound_bind.review_service.global.util.DateTimeUtils
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Repository

@Repository
class ReviewQueryRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
    private val elasticsearchClient: ElasticsearchClient,
): ReviewQueryRepository {

    override fun findReviewsOnMusic(
        musicId: Long,
        userId: Long,
        sort: ReviewSort,
        dto: ReviewCursorDTO?,
        pageable: Pageable
    ): Slice<ReviewQueryDTO> {
        val pageSize = pageable.pageSize
        val reviews = queryFactory
            .select(
                QReviewQueryDTO(
                    review.id,
                    review.userId,
                    review.userNickname,
                    review.userImageUrl,
                    review.message,
                    review.score,
                    review.commentNum,
                    review.likes,
                    review.createdAt,
                    review.updatedAt
                )
            )
            .from(review)
            .where(
                review.musicId.eq(musicId),
                reviewCursorLt(dto, sort)
            )
            .orderBy(sortReview(sort), review.id.desc())
            .limit(pageSize.toLong() + 1)
            .fetch()

        return processSliceQueryFromReviews(reviews, userId, pageable)
    }

    override fun findReviewsOnMusicWithElasticsearch(
        musicId: Long,
        userId: Long,
        sort: ReviewSort,
        dto: ReviewCursorDTO?,
        pageable: Pageable
    ): List<ReviewQueryDTO> {
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

        val pitResponse = elasticsearchClient.openPointInTime {
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

        val searchResponse = elasticsearchClient.search(
            searchRequestBuilder.build(),
            ReviewDocument::class.java
        )
        val metadata = searchResponse.hits()
        val reviews = metadata.hits().map { it.source() }

        return addIsLikedToReviewDocuments(
            reviews.map { ReviewQueryDTO(it!!) }.toMutableList(),
            userId
        )
    }

    private fun addIsLikedToReviewDocuments(
        reviews: MutableList<ReviewQueryDTO>,
        userId: Long
    ): MutableList<ReviewQueryDTO> {
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

        return reviews
    }

    private fun processSliceQueryFromReviews(
        reviews: MutableList<ReviewQueryDTO>,
        userId: Long,
        pageable: Pageable
    ): Slice<ReviewQueryDTO> {
        val updatedReviews = addIsLikedToReviewDocuments(reviews, userId)
        val pageSize = pageable.pageSize
        var hasNext = false

        if (updatedReviews.size > pageSize) {
            updatedReviews.removeAt(pageSize)
            hasNext = true
        }

        return SliceImpl(updatedReviews, pageable, hasNext)
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
                        val createdAtCursor = DateTimeUtils.convertStringToLocalDateTime(it.createdAtCursor)
                        return review.createdAt.lt(createdAtCursor)
                            .or(review.createdAt.eq(createdAtCursor).and(review.id.lt(it.idCursor)))
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