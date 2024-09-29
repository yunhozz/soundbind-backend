package com.sound_bind.pay_service.domain.application.charge.strategy

import com.sound_bind.pay_service.domain.application.charge.ChargeStrategy
import com.sound_bind.pay_service.domain.application.charge.handler.BankAccountChargeHandler
import com.sound_bind.pay_service.domain.application.dto.request.Bank

class BankAccountChargeStrategy(
    private val bank: Bank,
    private val accountNumber: String
): ChargeStrategy {

    private lateinit var chargeHandler: BankAccountChargeHandler

    override fun registerChargeHandler() {
        chargeHandler = when (bank) {
            Bank.KB -> TODO()
            Bank.NH -> TODO()
            Bank.WOORI -> TODO()
            Bank.HANA -> TODO()
            Bank.SHINHAN -> TODO()
        }
    }

    override fun chargePoint(amount: Int) {
        val acquired = chargeHandler.withdraw()
        if (acquired) chargeHandler.charge()
    }
}