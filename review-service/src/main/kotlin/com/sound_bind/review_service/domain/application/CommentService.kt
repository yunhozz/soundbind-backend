package com.sound_bind.review_service.domain.application

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sound_bind.review_service.domain.application.dto.request.KafkaRequestDTO
import com.sound_bind.review_service.domain.application.dto.response.CommentDetailsDTO
import com.sound_bind.review_service.domain.application.listener.CommentElasticsearchListener
import com.sound_bind.review_service.domain.persistence.entity.Comment
import com.sound_bind.review_service.domain.persistence.repository.CommentRepository
import com.sound_bind.review_service.domain.persistence.repository.ReviewRepository
import com.sound_bind.review_service.global.exception.CommentServiceException.CommentUpdateNotAuthorizedException
import com.sound_bind.review_service.global.exception.ReviewServiceException.ReviewNotFoundException
import com.sound_bind.review_service.global.util.RedisUtils
import khttp.post
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val reviewRepository: ReviewRepository,
    private val elasticsearchListener: CommentElasticsearchListener
) {

    @Transactional
    fun createComment(reviewId: Long, userId: Long, message: String): Long {
        val review = reviewRepository.findById(reviewId)
            .orElseThrow { ReviewNotFoundException("Review not found: $reviewId") }
            ?: throw IllegalArgumentException("Value is not Present by Key : user:$userId")
        val userInfo = getUserInformationOnRedis(userId)

        val comment = Comment.create(
            review,
            userId,
            userInfo["nickname"] as String,
            message
        )
        review.addComments(1)
        commentRepository.save(comment)
        elasticsearchListener.onCommentCreate(CommentDetailsDTO(comment, review))

        val notificationRequest = KafkaRequestDTO(
            topic = "comment-added-topic",
            message = KafkaRequestDTO.KafkaNotificationDTO(
                userId = review.userId,
                content = "${userInfo["nickname"] as String} 님이 당신의 리뷰에 댓글을 남겼습니다.",
                link = null
            )
        )
        sendMessageToKafkaProducer(notificationRequest)

        return comment.id!!
    }

    @Transactional
    fun deleteComment(commentId: Long, userId: Long) {
        val comment = (commentRepository.findWithReviewByIdAndUserId(commentId, userId)
            ?: throw CommentUpdateNotAuthorizedException("Not Authorized for Delete"))
        comment.softDelete() // subtract review's comment number
        elasticsearchListener.onCommentDelete(comment.id!!)
    }

    @Transactional
    fun deleteCommentsByUserWithdraw(userId: Long) {
        // TODO : userId 로 comment list 조회 -> 각 comment 마다 softDelete() 실행
    }

    private fun getUserInformationOnRedis(userId: Long) =
        RedisUtils.getJson("user:$userId", Map::class.java)
            ?: throw IllegalArgumentException("Value is not Present by Key : user:$userId")

    @Value("\${uris.kafka-server-uri:http://localhost:9000}/api/kafka")
    private lateinit var kafkaRequestUri: String

    private fun sendMessageToKafkaProducer(vararg request: KafkaRequestDTO) =
        post(
            url = kafkaRequestUri,
            headers = mapOf("Content-Type" to "application/json"),
            data = mapper.writeValueAsString(request.toList())
        )

    companion object {
        private val mapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
}