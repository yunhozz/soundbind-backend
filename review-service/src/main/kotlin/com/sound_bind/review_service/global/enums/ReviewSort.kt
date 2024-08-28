package com.sound_bind.review_service.global.enums

enum class ReviewSort(val key: String, val value: String, val target: String) {
    LIKES("likes", "인기순", "likes"),
    LATEST("latest", "최신순", "createdAt")
    ;

    companion object {
        fun of(key: String): ReviewSort = entries.find {
            it.key == key
        } ?: throw IllegalArgumentException("Unknown Sorting '$key'")
    }
}