package com.sound_bind.review_service.domain.application.listener

import com.sound_bind.review_service.domain.application.dto.response.CommentDetailsDTO

interface CommentElasticsearchListener {
    fun onCommentCreate(dto: CommentDetailsDTO)
    fun onCommentDelete(commentId: Long)
}