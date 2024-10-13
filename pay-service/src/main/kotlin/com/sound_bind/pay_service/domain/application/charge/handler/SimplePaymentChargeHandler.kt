package com.sound_bind.pay_service.domain.application.charge.handler

interface SimplePaymentChargeHandler {
    fun validate(email: String, phoneNumber: String): Boolean
    fun charge(pointAmount: Int)
}