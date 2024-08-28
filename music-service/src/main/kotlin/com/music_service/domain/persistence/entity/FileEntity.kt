package com.music_service.domain.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne

@Entity
class FileEntity private constructor(
    @Enumerated(EnumType.STRING)
    val fileType: FileType,
    val originalFileName: String,
    val savedName: String,
    val fileUrl: String,
): BaseEntity() {

    companion object {
        fun create(
            fileType: FileType,
            originalFileName: String,
            savedName: String,
            fileUrl: String,
            music: Music
        ): FileEntity {
            val fileEntity = FileEntity(fileType, originalFileName, savedName, fileUrl)
            fileEntity.music = music
            return fileEntity
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    var music: Music? = null
        protected set(music) {
            field = music
            music?.updateFiles(this)
        }

    enum class FileType {
        MUSIC, IMAGE
    }
}