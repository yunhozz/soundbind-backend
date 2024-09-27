package com.sound_bind.pay_service.domain.application.charge.strategy

interface ChargeStrategy {
    fun charge(amount: Int)
}