package com.sound_bind.pay_service.domain.application.charge.handler.bank_account

import com.sound_bind.global.utils.logger
import com.sound_bind.pay_service.domain.application.charge.handler.BankAccountChargeHandler

class NhBankAccountChargeHandler: BankAccountChargeHandler {

    private val log = logger()

    override fun validate(accountNumber: String, accountPassword: String): Boolean {
        log.info("NH Bank Validation Success for $accountNumber")
        return true
    }

    override fun withdraw(pointAmount: Int) {
        log.info("NH Bank Withdraw : $pointAmount")
    }

    override fun charge(pointAmount: Int) {
        log.info("NH Bank Charge : $pointAmount")
    }
}