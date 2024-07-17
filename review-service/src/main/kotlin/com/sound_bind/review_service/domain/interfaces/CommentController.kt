package com.sound_bind.review_service.domain.interfaces

import com.review_service.domain.interfaces.dto.APIResponse
import com.sound_bind.review_service.domain.application.CommentService
import com.sound_bind.review_service.global.dto.request.CommentCreateDTO
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/comments")
class CommentController(
    private val commentService: CommentService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun makeCommentOnReview(
        @RequestParam reviewId: String,
        @Valid @RequestBody dto: CommentCreateDTO
    ): APIResponse {
        val commentId = commentService.createComment(reviewId.toLong(), 789L, dto)
        return APIResponse.of("Comment Created", commentId)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getCommentListByReview(@RequestParam reviewId: String): APIResponse {
        val comments = commentService.findCommentListByReviewId(reviewId.toLong())
        return APIResponse.of("Comments in Review Found", comments)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCommentOnReview(@PathVariable("id") id: String): APIResponse {
        commentService.deleteComment(id.toLong(), 789L)
        return APIResponse.of("Comment Deleted")
    }
}