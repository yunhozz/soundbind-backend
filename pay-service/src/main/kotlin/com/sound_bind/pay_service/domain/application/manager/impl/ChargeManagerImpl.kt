package com.sound_bind.pay_service.domain.application.manager.impl

import com.sound_bind.pay_service.domain.application.charge.ChargeStrategy
import com.sound_bind.pay_service.domain.application.charge.strategy.BankAccountChargeStrategy
import com.sound_bind.pay_service.domain.application.charge.strategy.CreditCardChargeStrategy
import com.sound_bind.pay_service.domain.application.charge.strategy.SimplePaymentChargeStrategy
import com.sound_bind.pay_service.domain.application.dto.request.BankAccountDetails
import com.sound_bind.pay_service.domain.application.dto.request.CreditCardDetails
import com.sound_bind.pay_service.domain.application.dto.request.PointChargeRequestDTO
import com.sound_bind.pay_service.domain.application.dto.request.SimplePaymentDetails
import com.sound_bind.pay_service.domain.application.manager.ChargeManager
import com.sound_bind.pay_service.domain.persistence.entity.ChargeType
import com.sound_bind.pay_service.domain.persistence.entity.Point
import com.sound_bind.pay_service.domain.persistence.entity.PointCharge
import org.springframework.stereotype.Component

@Component
class ChargeManagerImpl: ChargeManager {

    private lateinit var strategy: ChargeStrategy

    override fun chargePoint(userId: Long, point: Point, dto: PointChargeRequestDTO): PointCharge {
        val chargeType = ChargeType.of(dto.chargeTypeDescription)
        val paymentDetails = dto.paymentDetails

        strategy = when (chargeType) {
            ChargeType.CREDIT_CARD -> {
                val creditCardDetails = paymentDetails as CreditCardDetails
                CreditCardChargeStrategy(
                    creditCardDetails.creditCard,
                    creditCardDetails.cardNumber,
                    creditCardDetails.cardExpirationDate
                )
            }
            ChargeType.BANK_ACCOUNT -> {
                val bankAccountDetails = paymentDetails as BankAccountDetails
                BankAccountChargeStrategy(
                    bankAccountDetails.bank,
                    bankAccountDetails.accountNumber,
                    bankAccountDetails.accountPassword
                )
            }
            ChargeType.SIMPLE_PAYMENT -> {
                val simplePaymentDetails = paymentDetails as SimplePaymentDetails
                SimplePaymentChargeStrategy(
                    simplePaymentDetails.provider,
                    simplePaymentDetails.email,
                    simplePaymentDetails.phoneNumber
                )
            }
        }

        val pointAmount = dto.originalAmount / 100 // TODO : 포인트 전환 정책 추가
        strategy.chargePoint(pointAmount)

        return PointCharge.createAndAddPoint(
            userId,
            point,
            ChargeType.of(dto.chargeTypeDescription),
            dto.originalAmount,
            pointAmount
        )
    }
}