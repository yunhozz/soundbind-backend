package com.sound_bind.pay_service.domain.application.charge.strategy

import com.sound_bind.pay_service.domain.application.charge.ChargeStrategy
import com.sound_bind.pay_service.domain.application.charge.handler.CreditCardChargeHandler
import com.sound_bind.pay_service.domain.application.dto.request.CreditCard
import java.util.Date

class CreditCardChargeStrategy(
    private val creditCard: CreditCard,
    private val cardNumber: String,
    private val cardExpirationDate: Date
): ChargeStrategy {

    private lateinit var chargeHandler: CreditCardChargeHandler

    override fun registerChargeHandler() {
        chargeHandler = when (creditCard) {
            CreditCard.MASTER -> TODO()
            CreditCard.VISA -> TODO()
            CreditCard.UNION_PAY -> TODO()
        }
    }

    override fun chargePoint(amount: Int) {
        val isValid = chargeHandler.validate(cardNumber, cardExpirationDate)
        if (isValid) chargeHandler.charge()
    }
}