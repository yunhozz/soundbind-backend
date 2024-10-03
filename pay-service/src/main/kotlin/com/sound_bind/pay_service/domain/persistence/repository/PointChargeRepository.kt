package com.sound_bind.pay_service.domain.persistence.repository

import com.sound_bind.pay_service.domain.persistence.entity.PointCharge
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PointChargeRepository: JpaRepository<PointCharge, Long> {
    @Query("select pc from PointCharge pc join pc.point p where p.id = :pointId")
    fun findAllByPointId(pointId: Long): List<PointCharge>
}