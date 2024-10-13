package com.sound_bind.pay_service.domain.persistence.repository

import com.sound_bind.pay_service.domain.persistence.entity.Point
import com.sound_bind.pay_service.domain.persistence.entity.PointCharge
import org.springframework.data.jpa.repository.JpaRepository

interface PointChargeRepository: JpaRepository<PointCharge, Long> {
    fun findAllByPoint(point: Point): List<PointCharge>
}