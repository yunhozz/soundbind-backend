package com.sound_bind.global.utils

object KafkaConstants {
    const val USER_ADDED_TOPIC = "user-added-topic"
    const val USER_DELETION_TOPIC = "user-deletion-topic"
    const val MUSIC_LIKE_TOPIC = "music-like-topic"
    const val MUSIC_REVIEW_TOPIC = "music-review-topic"
    const val MUSIC_NOT_FOUND_TOPIC = "music-not-found-topic"
    const val REVIEW_ADDED_TOPIC = "review-added-topic"
    const val REVIEW_LIKE_TOPIC = "review-like-topic"
    const val REVIEW_ROLLBACK_TOPIC = "review-rollback-topic"
    const val COMMENT_ADDED_TOPIC = "comment-added-topic"
    const val SPONSOR_RECEIVED_TOPIC = "sponsor-received-topic"

    const val MUSIC_SERVICE_GROUP = "music-service-group"
    const val REVIEW_SERVICE_GROUP = "review-service-group"
    const val COMMENT_SERVICE_GROUP = "comment-service-group"
    const val NOTIFICATION_SERVICE_GROUP = "notification-service-group"
    const val POINT_MANAGE_SERVICE_GROUP = "point-management-service-group"

    val KAFKA_TOPIC_LIST = listOf(
        USER_ADDED_TOPIC,
        USER_DELETION_TOPIC,
        MUSIC_LIKE_TOPIC,
        MUSIC_REVIEW_TOPIC,
        MUSIC_NOT_FOUND_TOPIC,
        REVIEW_ADDED_TOPIC,
        REVIEW_LIKE_TOPIC,
        REVIEW_ROLLBACK_TOPIC,
        COMMENT_ADDED_TOPIC,
        SPONSOR_RECEIVED_TOPIC
    )
}