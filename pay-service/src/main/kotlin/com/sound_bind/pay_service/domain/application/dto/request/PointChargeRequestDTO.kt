package com.sound_bind.pay_service.domain.application.dto.request

import com.sound_bind.pay_service.domain.application.charge.handler.Bank
import com.sound_bind.pay_service.domain.application.charge.handler.CreditCard
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.Date

data class PointChargeRequestDTO(
    @field:NotBlank
    val chargeTypeDescription: String,
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
    val email: String,
    val phoneNumber: String
): PaymentDetails