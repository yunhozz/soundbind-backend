package com.sound_bind.review_service.domain.application.manager

interface KafkaManager {
    fun sendMusicReviewCreateTopic(musicId: Long, reviewId: Long, nickname: String, oldScore: Double?, score: Double)
    fun sendMusicReviewUpdateTopic(musicId: Long, oldScore: Double?, score: Double)
    fun sendReviewLikeTopic(userId: Long, content: String, link: String? = null)
    fun sendCommentAddedTopic(userId: Long, content: String, link: String? = null)
}