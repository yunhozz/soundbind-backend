package com.sound_bind.review_service.domain.application.manager

interface KafkaManager {
    fun sendMusicReviewCreateTopic(musicId: Long, reviewId: Long, reviewerId: Long, nickname: String, oldScore: Double?, score: Double)
    fun sendMusicReviewUpdateTopic(musicId: Long, reviewId: Long, reviewerId: Long, oldScore: Double?, score: Double)
    fun sendReviewLikeTopic(userId: Long, content: String, link: String? = null)
    fun sendCommentAddedTopic(userId: Long, content: String, link: String? = null)
    fun sendMusicNotFoundTopic(userId: Long, content: String, link: String? = null)
}