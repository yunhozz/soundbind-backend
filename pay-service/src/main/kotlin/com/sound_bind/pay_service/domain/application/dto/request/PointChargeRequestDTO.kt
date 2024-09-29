package com.sound_bind.pay_service.domain.application.dto.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.Date

data class PointChargeRequestDTO(
    @field:NotBlank
    val chargeTypeDescription: String,
    @field:NotBlank
    val chargeMethod: ChargeMethod,
    @field:NotNull
    @field:Min(1)
    val originalAmount: Int,
    val paymentDetails: PaymentDetails
)

sealed interface PaymentDetails

data class CreditCardDetails(
    val creditCard: CreditCard,
    val cardNumber: String,
    val cardExpirationDate: Date
): PaymentDetails

data class BankAccountDetails(
    val bank: Bank,
    val accountNumber: String
): PaymentDetails

data class SimplePaymentDetails(
    val provider: SimplePaymentProvider,
    val email: String,
    val phoneNumber: String
): PaymentDetails

sealed interface ChargeMethod

enum class CreditCard: ChargeMethod {
    MASTER, VISA, UNION_PAY
}

enum class Bank: ChargeMethod {
    KB, NH, WOORI, HANA, SHINHAN
}

enum class SimplePaymentProvider: ChargeMethod {
    NAVER, KAKAO, TOSS
}