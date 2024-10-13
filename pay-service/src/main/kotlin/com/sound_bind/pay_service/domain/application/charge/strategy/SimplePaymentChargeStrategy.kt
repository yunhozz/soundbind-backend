package com.sound_bind.pay_service.domain.application.charge.strategy

import com.sound_bind.pay_service.domain.application.charge.ChargeStrategy
import com.sound_bind.pay_service.domain.application.charge.handler.factory.ChargeHandlerFactory
import com.sound_bind.pay_service.domain.application.dto.request.SimplePaymentProvider
import org.springframework.stereotype.Component

@Component
class SimplePaymentChargeStrategy(
    private val chargeHandlerFactory: ChargeHandlerFactory
): ChargeStrategy {

    lateinit var provider: SimplePaymentProvider
    lateinit var email: String
    lateinit var phoneNumber: String

    override fun chargePoint(pointAmount: Int) {
        val chargeHandler = chargeHandlerFactory.createSimplePaymentChargeHandler(provider)
        val isValid = chargeHandler.validate(email, phoneNumber)
        if (isValid) {
            chargeHandler.charge(pointAmount)
        }
    }
}