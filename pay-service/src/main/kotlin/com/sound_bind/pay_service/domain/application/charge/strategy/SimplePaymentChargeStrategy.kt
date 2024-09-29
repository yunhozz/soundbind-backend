package com.sound_bind.pay_service.domain.application.charge.strategy

import com.sound_bind.pay_service.domain.application.charge.ChargeStrategy
import com.sound_bind.pay_service.domain.application.charge.handler.SimplePaymentChargeHandler
import com.sound_bind.pay_service.domain.application.dto.request.SimplePaymentProvider

class SimplePaymentChargeStrategy(
    private val provider: SimplePaymentProvider,
    private val email: String,
    private val phoneNumber: String
): ChargeStrategy {

    private lateinit var chargeHandler: SimplePaymentChargeHandler

    override fun registerChargeHandler() {
        chargeHandler = when (provider) {
            SimplePaymentProvider.NAVER -> TODO()
            SimplePaymentProvider.KAKAO -> TODO()
            SimplePaymentProvider.TOSS -> TODO()
        }
    }

    override fun chargePoint(amount: Int) {
        chargeHandler.charge()
    }
}