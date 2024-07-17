package com.sound_bind.review_service.domain.persistence.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.sound_bind.review_service.domain.persistence.entity.QComment.comment
import com.sound_bind.review_service.domain.persistence.entity.QReview.review
import com.sound_bind.review_service.global.dto.response.CommentQueryDTO
import com.sound_bind.review_service.global.dto.response.QCommentQueryDTO
import org.springframework.stereotype.Repository

@Repository
class CommentQueryRepositoryImpl(private val queryFactory: JPAQueryFactory): CommentQueryRepository {

    override fun findCommentsByReviewId(reviewId: Long): List<CommentQueryDTO> =
        queryFactory
            .select(
                QCommentQueryDTO(
                    comment.userId,
                    comment.userNickname,
                    comment.message,
                    comment.createdAt,
                    comment.updatedAt
                )
            )
            .from(comment)
            .join(comment.review, review)
            .where(review.id.eq(reviewId))
            .orderBy(comment.createdAt.asc())
            .fetch()
}