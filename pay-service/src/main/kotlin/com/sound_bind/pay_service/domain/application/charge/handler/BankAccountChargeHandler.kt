package com.sound_bind.pay_service.domain.application.charge.handler

interface BankAccountChargeHandler {
    fun validate(accountNumber: String, accountPassword: String): Boolean
    fun withdraw(pointAmount: Int)
    fun charge(pointAmount: Int)
}