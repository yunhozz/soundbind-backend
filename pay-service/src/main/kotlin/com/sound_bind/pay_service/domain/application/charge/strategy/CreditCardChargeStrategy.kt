package com.sound_bind.pay_service.domain.application.charge.strategy

import com.sound_bind.pay_service.domain.application.charge.ChargeStrategy
import com.sound_bind.pay_service.domain.application.charge.handler.CreditCardChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.credit_card.MasterCardChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.credit_card.UnionPayChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.credit_card.VisaCardChargeHandler
import com.sound_bind.pay_service.domain.application.dto.request.CreditCard
import org.springframework.stereotype.Component
import java.util.Date

@Component
class CreditCardChargeStrategy: ChargeStrategy {

    lateinit var creditCard: CreditCard
    lateinit var cardNumber: String
    lateinit var cardExpirationDate: Date

    override fun chargePoint(pointAmount: Int) {
        val chargeHandler: CreditCardChargeHandler = when (creditCard) {
            CreditCard.MASTER -> MasterCardChargeHandler()
            CreditCard.VISA -> VisaCardChargeHandler()
            CreditCard.UNION_PAY -> UnionPayChargeHandler()
        }
        val isValid = chargeHandler.validate(cardNumber, cardExpirationDate)
        if (isValid) {
            chargeHandler.charge(pointAmount)
        }
    }
}