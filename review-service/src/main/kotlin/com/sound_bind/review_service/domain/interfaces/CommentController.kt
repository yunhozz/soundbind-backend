package com.sound_bind.review_service.domain.interfaces

import com.sound_bind.global.annotation.HeaderSubject
import com.sound_bind.global.dto.ApiResponse
import com.sound_bind.review_service.domain.application.CommentService
import com.sound_bind.review_service.domain.application.ElasticsearchService
import com.sound_bind.review_service.domain.persistence.es.CommentDocument
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
    ): ApiResponse<Long> {
        val commentId = commentService.createComment(reviewId.toLong(), sub.toLong(), message)
        return ApiResponse.of("Comment Created", commentId)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "리뷰에 대한 댓글 리스트 조회")
    fun getCommentListByReview(@RequestParam reviewId: String): ApiResponse<List<CommentDocument>> {
        val comments = elasticsearchService.findCommentsByReviewInElasticsearch(reviewId.toLong())
        return ApiResponse.of("Comments in Review Found", comments)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "댓글 삭제")
    fun deleteCommentOnReview(
        @HeaderSubject sub: String,
        @PathVariable("id") id: String
    ): ApiResponse<Unit> {
        commentService.deleteComment(id.toLong(), sub.toLong())
        return ApiResponse.of("Comment Deleted")
    }
}