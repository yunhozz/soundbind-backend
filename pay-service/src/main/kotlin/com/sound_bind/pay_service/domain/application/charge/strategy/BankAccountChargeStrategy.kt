package com.sound_bind.pay_service.domain.application.charge.strategy

import com.sound_bind.pay_service.domain.application.charge.ChargeStrategy
import com.sound_bind.pay_service.domain.application.charge.handler.factory.ChargeHandlerFactory
import com.sound_bind.pay_service.domain.application.dto.request.Bank
import org.springframework.stereotype.Component

@Component
class BankAccountChargeStrategy(
    private val chargeHandlerFactory: ChargeHandlerFactory
): ChargeStrategy {

    lateinit var bank: Bank
    lateinit var accountNumber: String
    lateinit var accountPassword: String

    override fun chargePoint(pointAmount: Int) {
        val chargeHandler = chargeHandlerFactory.createBankAccountChargeHandler(bank)
        val isValid = chargeHandler.validate(accountNumber, accountPassword)
        if (isValid) {
            chargeHandler.withdraw(pointAmount)
            chargeHandler.charge(pointAmount)
        }
    }
}