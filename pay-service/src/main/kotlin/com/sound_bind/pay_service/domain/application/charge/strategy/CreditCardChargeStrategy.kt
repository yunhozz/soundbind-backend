package com.sound_bind.pay_service.domain.application.charge.strategy

import com.sound_bind.pay_service.domain.application.charge.ChargeStrategy
import com.sound_bind.pay_service.domain.application.charge.handler.factory.ChargeHandlerFactory
import com.sound_bind.pay_service.domain.application.dto.request.CreditCard
import org.springframework.stereotype.Component
import java.util.Date

@Component
class CreditCardChargeStrategy(
    private val chargeHandlerFactory: ChargeHandlerFactory
): ChargeStrategy {

    lateinit var creditCard: CreditCard
    lateinit var cardNumber: String
    lateinit var cardExpirationDate: Date

    override fun chargePoint(pointAmount: Int) {
        val chargeHandler = chargeHandlerFactory.createCreditCardChargeHandler(creditCard)
        val isValid = chargeHandler.validate(cardNumber, cardExpirationDate)
        if (isValid) {
            chargeHandler.charge(pointAmount)
        }
    }
}