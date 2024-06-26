package com.music_service.domain.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

@Entity
class Music(
    val userId: Long,
    title: String,
    userNickname: String,
    musicUrl: String,
    imageUrl: String? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @CreatedDate
    val createdAt: LocalDateTime? = null

    @LastModifiedDate
    var updatedAt: LocalDateTime? = null

    var title: String = title
        protected set
    var userNickname: String = userNickname
        protected set
    var musicUrl: String = musicUrl
        protected set
    var imageUrl: String? = imageUrl
        protected set

    fun updateTitle(title: String) {
        this.title = title
    }

    fun updateImageUrl(url: String) {
        this.imageUrl = url
    }
}