package com.sound_bind.pay_service.domain.application.charge.handler.factory

import com.sound_bind.pay_service.domain.application.charge.handler.BankAccountChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.CreditCardChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.SimplePaymentChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.bank_account.HanaBankAccountChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.bank_account.KbBankAccountChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.bank_account.NhBankAccountChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.bank_account.ShinhanBankAccountChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.bank_account.WooriBankAccountChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.credit_card.MasterCardChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.credit_card.UnionPayChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.credit_card.VisaCardChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.simple_payment.KakaoSimplePaymentChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.simple_payment.NaverSimplePaymentChargeHandler
import com.sound_bind.pay_service.domain.application.charge.handler.simple_payment.TossSimplePaymentChargeHandler
import com.sound_bind.pay_service.domain.application.dto.request.Bank
import com.sound_bind.pay_service.domain.application.dto.request.CreditCard
import com.sound_bind.pay_service.domain.application.dto.request.SimplePaymentProvider
import org.springframework.stereotype.Component

@Component
class ChargeHandlerFactoryImpl: ChargeHandlerFactory {

    override fun createBankAccountChargeHandler(bank: Bank): BankAccountChargeHandler =
        when (bank) {
            Bank.KB -> KbBankAccountChargeHandler()
            Bank.NH -> NhBankAccountChargeHandler()
            Bank.WOORI -> WooriBankAccountChargeHandler()
            Bank.HANA -> HanaBankAccountChargeHandler()
            Bank.SHINHAN -> ShinhanBankAccountChargeHandler()
        }

    override fun createCreditCardChargeHandler(creditCard: CreditCard): CreditCardChargeHandler =
        when (creditCard) {
            CreditCard.MASTER -> MasterCardChargeHandler()
            CreditCard.VISA -> VisaCardChargeHandler()
            CreditCard.UNION_PAY -> UnionPayChargeHandler()
        }

    override fun createSimplePaymentChargeHandler(provider: SimplePaymentProvider): SimplePaymentChargeHandler =
        when (provider) {
            SimplePaymentProvider.NAVER -> NaverSimplePaymentChargeHandler()
            SimplePaymentProvider.KAKAO -> KakaoSimplePaymentChargeHandler()
            SimplePaymentProvider.TOSS -> TossSimplePaymentChargeHandler()
        }
}