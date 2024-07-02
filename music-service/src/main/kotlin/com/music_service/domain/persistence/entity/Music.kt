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
class Music(
    val userId: Long,
    userNickname: String,
    title: String,
    genres: Set<Genre>
): BaseEntity() {

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

    lateinit var deletedAt: LocalDateTime

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "music", orphanRemoval = true)
    var files: MutableList<FileEntity> = mutableListOf()
        protected set

    fun updateGenres(genres: Set<Genre>) {
        this.genres = genres
    }

    fun updateTitle(title: String) {
        this.title = title
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