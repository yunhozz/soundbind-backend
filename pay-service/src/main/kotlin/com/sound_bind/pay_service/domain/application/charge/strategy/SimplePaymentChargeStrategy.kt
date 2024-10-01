package com.sound_bind.pay_service.domain.application.charge.strategy

import com.sound_bind.pay_service.domain.application.charge.ChargeStrategy
import com.sound_bind.pay_service.domain.application.charge.handler.SimplePaymentChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.simple_payment.KakaoSimplePaymentChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.simple_payment.NaverSimplePaymentChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.simple_payment.TossSimplePaymentChargeHandler
import com.sound_bind.pay_service.domain.application.dto.request.SimplePaymentProvider
import org.springframework.stereotype.Component

@Component
class SimplePaymentChargeStrategy: ChargeStrategy {

    lateinit var provider: SimplePaymentProvider
    lateinit var email: String
    lateinit var phoneNumber: String

    override fun chargePoint(pointAmount: Int) {
        val chargeHandler: SimplePaymentChargeHandler = when (provider) {
            SimplePaymentProvider.NAVER -> NaverSimplePaymentChargeHandler()
            SimplePaymentProvider.KAKAO -> KakaoSimplePaymentChargeHandler()
            SimplePaymentProvider.TOSS -> TossSimplePaymentChargeHandler()
        }
        val isValid = chargeHandler.validate(email, phoneNumber)
        if (isValid) {
            chargeHandler.charge(pointAmount)
        }
    }
}