package com.music_service.domain.application.manager

interface KafkaManager {
    fun sendMusicLikeTopic(userId: Long, content: String)
    fun sendReviewAddedTopic(userId: Long, content: String, link: String)
}