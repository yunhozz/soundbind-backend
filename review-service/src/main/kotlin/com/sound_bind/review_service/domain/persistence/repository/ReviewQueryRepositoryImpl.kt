package com.sound_bind.review_service.domain.persistence.repository

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.sound_bind.review_service.domain.persistence.entity.QReview.review
import com.sound_bind.review_service.domain.persistence.entity.QReviewLikes.reviewLikes
import com.sound_bind.review_service.domain.persistence.repository.ReviewQueryRepository.ReviewSort
import com.sound_bind.review_service.domain.persistence.repository.dto.QReviewLikesQueryDTO
import com.sound_bind.review_service.domain.persistence.repository.dto.QReviewQueryDTO
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewCursorRequestDTO
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewLikesQueryDTO
import com.sound_bind.review_service.domain.persistence.repository.dto.ReviewQueryDTO
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class ReviewQueryRepositoryImpl(private val queryFactory: JPAQueryFactory): ReviewQueryRepository {

    override fun findReviewsOnMusic(
        musicId: Long,
        userId: Long,
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
                    review.comments,
                    review.likes,
                    review.createdAt,
                    review.updatedAt
                )
            )
            .from(review)
            .where(
                review.musicId.eq(musicId),
                reviewCursorLt(dto.idCursor, dto.likesCursor, dto.createdAtCursor, sort)
            )
            .orderBy(sortReview(sort), review.id.desc())
            .limit(pageSize.toLong() + 1)
            .fetch()

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