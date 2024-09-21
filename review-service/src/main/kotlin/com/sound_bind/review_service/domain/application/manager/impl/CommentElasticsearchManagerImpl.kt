package com.sound_bind.review_service.domain.application.manager.impl

import com.sound_bind.review_service.domain.application.ElasticsearchService
import com.sound_bind.review_service.domain.application.dto.response.CommentDetailsDTO
import com.sound_bind.review_service.domain.application.manager.CommentElasticsearchManager
import org.springframework.stereotype.Component

@Component
class CommentElasticsearchManagerImpl(
    private val elasticsearchService: ElasticsearchService
): CommentElasticsearchManager {

    override fun onCommentCreate(dto: CommentDetailsDTO) =
        elasticsearchService.saveCommentInElasticSearch(dto)

    override fun onCommentDelete(commentId: Long) =
        elasticsearchService.deleteCommentInElasticSearch(commentId)
}