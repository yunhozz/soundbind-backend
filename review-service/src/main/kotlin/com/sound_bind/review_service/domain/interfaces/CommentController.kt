package com.sound_bind.review_service.domain.interfaces

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.review_service.domain.interfaces.dto.APIResponse
import com.sound_bind.review_service.domain.application.CommentService
import com.sound_bind.review_service.domain.interfaces.dto.KafkaRecordDTO
import com.sound_bind.review_service.global.annotation.HeaderSubject
import khttp.post
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/comments")
class CommentController(private val commentService: CommentService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun makeCommentOnReview(
        @HeaderSubject sub: String,
        @RequestParam reviewId: String,
        @RequestParam message: String,
    ): APIResponse {
        val result = commentService.createComment(reviewId.toLong(), sub.toLong(), message)
        val myInfo = commentService.getUserInformationOnRedis(sub.toLong())
        val record = KafkaRecordDTO(
            "comment-added-topic",
            result.reviewerId.toString(),
            "${myInfo["nickname"] as String} 님이 당신의 리뷰에 댓글을 남겼습니다.",
            null
        )
        post(
            url = "http://localhost:9000/api/kafka",
            headers = mapOf("Content-Type" to "application/json"),
            data = jacksonObjectMapper().writeValueAsString(record)
        )
        return APIResponse.of("Comment Created", result.commentId)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getCommentListByReview(@RequestParam reviewId: String): APIResponse {
        val comments = commentService.findCommentListByReviewId(reviewId.toLong())
        return APIResponse.of("Comments in Review Found", comments)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCommentOnReview(@HeaderSubject sub: String, @PathVariable("id") id: String): APIResponse {
        commentService.deleteComment(id.toLong(), sub.toLong())
        return APIResponse.of("Comment Deleted")
    }
}