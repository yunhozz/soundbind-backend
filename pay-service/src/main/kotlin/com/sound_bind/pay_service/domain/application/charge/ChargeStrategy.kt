package com.sound_bind.pay_service.domain.application.charge

interface ChargeStrategy {
    fun chargePoint(pointAmount: Int)
}