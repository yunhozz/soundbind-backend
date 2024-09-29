package com.sound_bind.pay_service.domain.application.charge.handler

import java.util.Date

interface CreditCardChargeHandler {
    fun validate(cardNumber: String, cardExpirationDate: Date): Boolean
    fun charge()
}