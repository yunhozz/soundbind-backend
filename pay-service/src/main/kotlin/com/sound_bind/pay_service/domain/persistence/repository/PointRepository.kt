package com.sound_bind.pay_service.domain.persistence.repository

import com.sound_bind.pay_service.domain.persistence.entity.Point
import org.springframework.data.jpa.repository.JpaRepository

interface PointRepository: JpaRepository<Point, Long> {
    fun findPointByUserId(userId: Long): Point?
}