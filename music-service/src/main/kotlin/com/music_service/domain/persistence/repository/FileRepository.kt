package com.music_service.domain.persistence.repository

import com.music_service.domain.persistence.entity.FileEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface FileRepository: JpaRepository<FileEntity, Long> {

    @Query("select f from FileEntity f join f.music m where m.id = :musicId")
    fun findFilesWhereMusicId(musicId: Long): List<FileEntity>
}