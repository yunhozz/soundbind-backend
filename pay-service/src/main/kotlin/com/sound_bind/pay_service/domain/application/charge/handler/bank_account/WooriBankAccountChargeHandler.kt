package com.sound_bind.pay_service.domain.application.charge.handler.bank_account

import com.sound_bind.global.utils.logger
import com.sound_bind.pay_service.domain.application.charge.handler.BankAccountChargeHandler

class WooriBankAccountChargeHandler: BankAccountChargeHandler {

    private val log = logger()

    override fun validate(accountNumber: String, accountPassword: String): Boolean {
        log.info("Woori Bank Validation Success for $accountNumber")
        return true
    }

    override fun withdraw(pointAmount: Int) {
        log.info("Woori Bank Withdraw : $pointAmount")
    }

    override fun charge(pointAmount: Int) {
        log.info("Woori Bank Charge : $pointAmount")
    }
}