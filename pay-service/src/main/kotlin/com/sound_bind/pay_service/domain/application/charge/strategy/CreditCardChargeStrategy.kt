package com.sound_bind.pay_service.domain.application.charge.strategy

import com.sound_bind.pay_service.domain.application.charge.ChargeStrategy
import com.sound_bind.pay_service.domain.application.charge.handler.CreditCardChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.credit_card.MasterCardChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.credit_card.UnionPayChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.credit_card.VisaCardChargeHandler
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
            CreditCard.MASTER -> MasterCardChargeHandler()
            CreditCard.VISA -> VisaCardChargeHandler()
            CreditCard.UNION_PAY -> UnionPayChargeHandler()
        }
    }

    override fun chargePoint(pointAmount: Int) {
        val isValid = chargeHandler.validate(cardNumber, cardExpirationDate)
        if (isValid) {
            chargeHandler.charge(pointAmount)
        }
    }
}