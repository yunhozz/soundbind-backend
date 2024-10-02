package com.sound_bind.pay_service.domain.application.charge.handler.factory

import com.sound_bind.pay_service.domain.application.charge.handler.BankAccountChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.CreditCardChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.SimplePaymentChargeHandler
import com.sound_bind.pay_service.domain.application.dto.request.Bank
import com.sound_bind.pay_service.domain.application.dto.request.CreditCard
import com.sound_bind.pay_service.domain.application.dto.request.SimplePaymentProvider

interface ChargeHandlerFactory {
    fun createBankAccountChargeHandler(bank: Bank): BankAccountChargeHandler
    fun createCreditCardChargeHandler(creditCard: CreditCard): CreditCardChargeHandler
    fun createSimplePaymentChargeHandler(provider: SimplePaymentProvider): SimplePaymentChargeHandler
}