package com.sound_bind.pay_service.domain.application.manager.impl

import com.sound_bind.pay_service.domain.application.charge.strategy.ChargeStrategy
import com.sound_bind.pay_service.domain.application.dto.request.PointChargeRequestDTO
import com.sound_bind.pay_service.domain.application.manager.ChargeManager
import com.sound_bind.pay_service.domain.persistence.entity.ChargeType
import com.sound_bind.pay_service.domain.persistence.entity.Point
import com.sound_bind.pay_service.domain.persistence.entity.PointCharge
import org.springframework.stereotype.Component

@Component
class ChargeManagerImpl: ChargeManager {

    private lateinit var strategy: ChargeStrategy

    override fun registerStrategy(strategy: ChargeStrategy) {
        this.strategy = strategy
    }

    override fun chargePoint(userId: Long, point: Point, dto: PointChargeRequestDTO): PointCharge {
        // TODO: 결제 방식에 따른 포인트 충전 정책 적용
        val pointAmount = dto.originalAmount
        return PointCharge.createAndAddPoint(
            userId,
            point,
            ChargeType.of(dto.chargeTypeDescription),
            dto.originalAmount,
            pointAmount
        )
    }
}