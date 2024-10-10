package com.sound_bind.pay_service.domain.application.charge.handler.credit_card

import com.sound_bind.global.utils.logger
import com.sound_bind.pay_service.domain.application.charge.handler.CreditCardChargeHandler
import java.util.Date

class MasterCardChargeHandler: CreditCardChargeHandler {

    private val log = logger()

    override fun validate(cardNumber: String, cardExpirationDate: Date): Boolean {
        log.info("Master Card Validation Success for $cardNumber")
        return true
    }

    override fun charge(pointAmount: Int) {
        log.info("Master Card Charge : $pointAmount")
    }
}