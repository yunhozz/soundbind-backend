package com.sound_bind.review_service.domain.application.manager

import com.sound_bind.review_service.domain.application.dto.response.CommentDetailsDTO

interface CommentElasticsearchManager {
    fun onCommentCreate(dto: CommentDetailsDTO)
    fun onCommentDelete(commentId: Long)
}