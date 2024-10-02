package com.sound_bind.pay_service.domain.application.charge.handler.bank_account

import com.sound_bind.pay_service.domain.application.charge.handler.BankAccountChargeHandler
import com.sound_bind.pay_service.global.util.logger

class HanaBankAccountChargeHandler: BankAccountChargeHandler {

    private val log = logger()

    override fun validate(accountNumber: String, accountPassword: String): Boolean {
        log.info("Hana Bank Validation Success for $accountNumber")
        return true
    }

    override fun withdraw(pointAmount: Int) {
        log.info("Hana Bank Withdraw : $pointAmount")
    }

    override fun charge(pointAmount: Int) {
        log.info("Hana Bank Charge : $pointAmount")
    }
}