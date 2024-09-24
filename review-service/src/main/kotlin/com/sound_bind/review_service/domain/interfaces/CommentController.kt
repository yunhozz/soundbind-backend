package com.sound_bind.review_service.domain.interfaces

import com.review_service.domain.interfaces.dto.APIResponse
import com.sound_bind.review_service.domain.application.CommentService
import com.sound_bind.review_service.domain.application.ElasticsearchService
import com.sound_bind.review_service.global.annotation.HeaderSubject
import io.swagger.v3.oas.annotations.Operation
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
class CommentController(
    private val commentService: CommentService,
    private val elasticsearchService: ElasticsearchService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "리뷰에 대한 댓글 작성")
    fun makeCommentOnReview(
        @HeaderSubject sub: String,
        @RequestParam reviewId: String,
        @RequestParam message: String,
    ): APIResponse {
        val commentId = commentService.createComment(reviewId.toLong(), sub.toLong(), message)
        return APIResponse.of("Comment Created", commentId)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "리뷰에 대한 댓글 리스트 조회")
    fun getCommentListByReview(@RequestParam reviewId: String): APIResponse {
        val comments = elasticsearchService.findCommentsByReviewInElasticsearch(reviewId.toLong())
        return APIResponse.of("Comments in Review Found", comments)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "댓글 삭제")
    fun deleteCommentOnReview(@HeaderSubject sub: String, @PathVariable("id") id: String): APIResponse {
        commentService.deleteComment(id.toLong(), sub.toLong())
        return APIResponse.of("Comment Deleted")
    }
}