package com.sound_bind.review_service.domain.application.listener.impl

import com.sound_bind.review_service.domain.application.ElasticsearchService
import com.sound_bind.review_service.domain.application.dto.response.CommentDetailsDTO
import com.sound_bind.review_service.domain.application.listener.CommentElasticsearchListener
import org.springframework.stereotype.Component

@Component
class CommentElasticsearchListenerImpl(
    private val elasticsearchService: ElasticsearchService
): CommentElasticsearchListener {

    override fun onCommentCreate(dto: CommentDetailsDTO) =
        elasticsearchService.saveCommentInElasticSearch(dto)

    override fun onCommentDelete(commentId: Long) =
        elasticsearchService.deleteCommentInElasticSearch(commentId)
}