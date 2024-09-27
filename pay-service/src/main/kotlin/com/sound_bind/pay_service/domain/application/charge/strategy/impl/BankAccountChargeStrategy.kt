package com.sound_bind.pay_service.domain.application.charge.strategy.impl

import com.sound_bind.pay_service.domain.application.charge.handler.Bank
import com.sound_bind.pay_service.domain.application.charge.strategy.ChargeStrategy

class BankAccountChargeStrategy(
    private val bank: Bank,
    private val accountNumber: String,
//    private val chargeHandler: BankChargeHandler
): ChargeStrategy {

    override fun charge(amount: Int) {
        TODO("Not yet implemented")
    }
}