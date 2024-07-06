package com.sound_bind.review_service.domain.persistence.repository

import com.sound_bind.review_service.domain.persistence.entity.Comment
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository: JpaRepository<Comment, Long>