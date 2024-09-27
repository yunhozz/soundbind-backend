package com.sound_bind.pay_service.domain.application.charge.strategy.impl

import com.sound_bind.pay_service.domain.application.charge.handler.CreditCardChargeHandler
import com.sound_bind.pay_service.domain.application.charge.strategy.ChargeStrategy
import java.util.Date

class CreditCardChargeStrategy(
    private val cardNumber: String,
    private val cardExpirationDate: Date,
//    private val chargeHandler: CreditCardChargeHandler
): ChargeStrategy {

    override fun charge(amount: Int) {
        TODO("Not yet implemented")
    }
}