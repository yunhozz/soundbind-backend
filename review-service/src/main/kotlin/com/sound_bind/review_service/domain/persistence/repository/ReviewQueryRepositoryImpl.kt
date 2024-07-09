package com.sound_bind.review_service.domain.persistence.repository

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sound_bind.review_service.domain.persistence.entity.QComment.comment
import com.sound_bind.review_service.domain.persistence.entity.QReview.review
import com.sound_bind.review_service.domain.persistence.repository.ReviewQueryRepository.ReviewSort
import com.sound_bind.review_service.global.dto.request.ReviewCursorRequestDTO
import com.sound_bind.review_service.global.dto.response.QReviewQueryDTO
import com.sound_bind.review_service.global.dto.response.ReviewQueryDTO
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class ReviewQueryRepositoryImpl(private val queryFactory: JPAQueryFactory): ReviewQueryRepository {

    override fun findReviewsOnMusic(
        musicId: Long,
        sort: ReviewSort,
        dto: ReviewCursorRequestDTO,
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
                    review.likes,
                    comment.count().castToNum(Int::class.java),
                    review.createdAt,
                    review.updatedAt
                )
            )
            .from(comment)
            .join(comment.review, review)
            .where(
                review.musicId.eq(musicId),
                reviewCursorLt(dto.idCursor, dto.likesCursor, dto.createdAtCursor, sort)
            )
            .orderBy(sortReview(sort), review.id.desc())
            .limit(pageSize.toLong() + 1)
            .fetch()

        var hasNext = false
        if (reviews.size > pageSize) {
            reviews.removeAt(pageSize)
            hasNext = true
        }

        return SliceImpl(reviews, pageable, hasNext)
    }

    private fun reviewCursorLt(
        idCursor: Long?,
        likesCursor: Int?,
        createdAtCursor: LocalDateTime?,
        sort: ReviewSort
    ): BooleanExpression? {
        when (sort) {
            ReviewSort.LIKES ->
                if (idCursor != null && likesCursor != null) {
                    return review.likes.lt(likesCursor)
                        .or(review.likes.eq(likesCursor).and(review.id.lt(idCursor)))
                }
            ReviewSort.LATEST ->
                if (idCursor != null && createdAtCursor != null) {
                    return review.createdAt.lt(createdAtCursor)
                        .or(review.createdAt.eq(createdAtCursor).and(review.id.lt(idCursor)))
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