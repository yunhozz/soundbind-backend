package com.sound_bind.pay_service.domain.application.charge.strategy.impl

import com.sound_bind.pay_service.domain.application.charge.strategy.ChargeStrategy

class SimplePaymentChargeStrategy(
    private val email: String,
    private val phoneNumber: String,
//    private val chargeHandler: SimplePaymentChargeHandler
): ChargeStrategy {

    override fun charge(amount: Int) {
        TODO("Not yet implemented")
    }
}