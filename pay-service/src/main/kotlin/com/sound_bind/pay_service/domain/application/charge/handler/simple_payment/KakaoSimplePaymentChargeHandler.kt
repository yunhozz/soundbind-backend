package com.sound_bind.pay_service.domain.application.charge.handler.simple_payment

import com.sound_bind.pay_service.domain.application.charge.handler.SimplePaymentChargeHandler
import com.sound_bind.pay_service.global.util.logger

class KakaoSimplePaymentChargeHandler: SimplePaymentChargeHandler {

    private val log = logger()

    override fun validate(email: String, phoneNumber: String): Boolean {
        log.info("Kakao Simple Payment Validation Success for $email")
        return true
    }

    override fun charge(pointAmount: Int) {
        log.info("Kakao Simple Payment Charge : $pointAmount")
    }
}