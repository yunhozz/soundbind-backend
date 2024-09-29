package com.sound_bind.pay_service.domain.application.charge.strategy

import com.sound_bind.pay_service.domain.application.charge.ChargeStrategy
import com.sound_bind.pay_service.domain.application.charge.handler.BankAccountChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.bank_account.HanaBankAccountChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.bank_account.KbBankAccountChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.bank_account.NhBankAccountChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.bank_account.ShinhanBankAccountChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.bank_account.WooriBankAccountChargeHandler
import com.sound_bind.pay_service.domain.application.dto.request.Bank

class BankAccountChargeStrategy(
    private val bank: Bank,
    private val accountNumber: String,
    private val accountPassword: String
): ChargeStrategy {

    private lateinit var chargeHandler: BankAccountChargeHandler

    override fun registerChargeHandler() {
        chargeHandler = when (bank) {
            Bank.KB -> KbBankAccountChargeHandler()
            Bank.NH -> NhBankAccountChargeHandler()
            Bank.WOORI -> WooriBankAccountChargeHandler()
            Bank.HANA -> HanaBankAccountChargeHandler()
            Bank.SHINHAN -> ShinhanBankAccountChargeHandler()
        }
    }

    override fun chargePoint(pointAmount: Int) {
        val isValid = chargeHandler.validate(accountNumber, accountPassword)
        if (isValid) {
            chargeHandler.withdraw(pointAmount)
            chargeHandler.charge(pointAmount)
        }
    }
}