package com.sound_bind.pay_service.domain.application.manager

import com.sound_bind.pay_service.domain.application.dto.request.PointChargeRequestDTO
import com.sound_bind.pay_service.domain.persistence.entity.Point
import com.sound_bind.pay_service.domain.persistence.entity.PointCharge

interface ChargeManager {
    fun chargePoint(userId: Long, point: Point, dto: PointChargeRequestDTO): PointCharge
}