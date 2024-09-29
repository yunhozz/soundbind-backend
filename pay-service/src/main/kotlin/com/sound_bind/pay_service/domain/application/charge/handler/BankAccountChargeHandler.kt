package com.sound_bind.pay_service.domain.application.charge.handler

interface BankAccountChargeHandler {
    fun withdraw(): Boolean
    fun charge()
}