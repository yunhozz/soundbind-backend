package com.sound_bind.pay_service.domain.application.charge.handler.credit_card

import com.sound_bind.global.utils.logger
import com.sound_bind.pay_service.domain.application.charge.handler.CreditCardChargeHandler
import java.util.Date

class UnionPayChargeHandler: CreditCardChargeHandler {

    private val log = logger()

    override fun validate(cardNumber: String, cardExpirationDate: Date): Boolean {
        log.info("Union Pay Validation Success for $cardNumber")
        return true
    }

    override fun charge(pointAmount: Int) {
        log.info("Union Pay Charge : $pointAmount")
    }
}