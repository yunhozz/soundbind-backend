package com.sound_bind.pay_service.domain.application.charge.strategy

import com.sound_bind.pay_service.domain.application.charge.ChargeStrategy
import com.sound_bind.pay_service.domain.application.charge.handler.SimplePaymentChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.simple_payment.KakaoSimplePaymentChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.simple_payment.NaverSimplePaymentChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.simple_payment.TossSimplePaymentChargeHandler
import com.sound_bind.pay_service.domain.application.dto.request.SimplePaymentProvider

class SimplePaymentChargeStrategy(
    private val provider: SimplePaymentProvider,
    private val email: String,
    private val phoneNumber: String
): ChargeStrategy {

    private lateinit var chargeHandler: SimplePaymentChargeHandler

    override fun registerChargeHandler() {
        chargeHandler = when (provider) {
            SimplePaymentProvider.NAVER -> NaverSimplePaymentChargeHandler()
            SimplePaymentProvider.KAKAO -> KakaoSimplePaymentChargeHandler()
            SimplePaymentProvider.TOSS -> TossSimplePaymentChargeHandler()
        }
    }

    override fun chargePoint(pointAmount: Int) {
        val isValid = chargeHandler.validate(email, phoneNumber)
        if (isValid) {
            chargeHandler.charge(pointAmount)
        }
    }
}