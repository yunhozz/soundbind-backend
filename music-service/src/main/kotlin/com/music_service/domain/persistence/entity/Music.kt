package com.music_service.domain.persistence.entity

import jakarta.persistence.CollectionTable
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

@Entity
@SQLRestriction("deleted_at is null")
class Music private constructor(
    val userId: Long,
    userNickname: String,
    title: String,
    genres: Set<Genre>
): BaseEntity() {

    companion object {
        fun create(
            userId: Long,
            userNickname: String,
            title: String,
            genres: Set<Genre>
        ): Music
            = Music(userId, userNickname, title, genres)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    var userNickname: String = userNickname
        protected set

    var title: String = title
        protected set

    @ElementCollection(fetch = FetchType.LAZY, targetClass = Genre::class)
    @CollectionTable(joinColumns = [JoinColumn(name = "music_id")], name = "music_genre")
    @Enumerated(EnumType.STRING)
    var genres: Set<Genre> = genres
        protected set

    var deletedAt: LocalDateTime? = null

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "music", orphanRemoval = true)
    val files: MutableList<FileEntity> = mutableListOf()

    fun updateInfo(title: String, genres: Set<Genre>) {
        this.title = title
        this.genres = genres
    }

    fun softDelete() {
        deletedAt ?: run { deletedAt = LocalDateTime.now() }
    }

    enum class Genre(
        val genreName: String
    ) {
        CLASSIC("클래식"),
        JAZZ("재즈"),
        POP("팝"),
        BALLAD("발라드"),
        HIPHOP("힙합"),
        COUNTRY("컨트리"),
        DISCO("디스코"),
        ROCK("락"),
        ELECTRONIC("일렉트로닉스"),
        TROT("트로트")
        ;

        companion object {
            fun of(genreName: String): Genre = entries.find {
                it.name == genreName
            } ?: throw IllegalArgumentException("Unknown genre '$genreName'")
        }
    }
}