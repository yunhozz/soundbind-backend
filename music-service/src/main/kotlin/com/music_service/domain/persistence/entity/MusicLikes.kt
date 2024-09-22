package com.music_service.domain.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne

@Entity
class MusicLikes(
    @ManyToOne(fetch = FetchType.LAZY)
    val music: Music,
    val userId: Long,
    flag: Boolean = true
): BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    var flag = flag
        protected set

    fun changeFlag() {
        flag = flag.not()
        music.apply { if (flag) addLikes(1) else subtractLikes(1) }
    }
}