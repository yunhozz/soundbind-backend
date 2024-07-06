package com.sound_bind.review_service.domain.persistence.repository

import com.sound_bind.review_service.domain.persistence.entity.Review
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewRepository: JpaRepository<Review, Long>