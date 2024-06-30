package com.music_service.domain.persistence.repository

import com.music_service.domain.persistence.entity.FileEntity
import org.springframework.data.jpa.repository.JpaRepository

interface FileRepository: JpaRepository<FileEntity, Long>