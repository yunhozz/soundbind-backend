package com.sound_bind.pay_service.domain.application

import com.sound_bind.pay_service.domain.application.charge.strategy.impl.BankAccountChargeStrategy
import com.sound_bind.pay_service.domain.application.charge.strategy.impl.CreditCardChargeStrategy
import com.sound_bind.pay_service.domain.application.charge.strategy.impl.SimplePaymentChargeStrategy
import com.sound_bind.pay_service.domain.application.dto.message.UserSignUpMessageDTO
import com.sound_bind.pay_service.domain.application.dto.request.BankAccountDetails
import com.sound_bind.pay_service.domain.application.dto.request.CreditCardDetails
import com.sound_bind.pay_service.domain.application.dto.request.PointChargeRequestDTO
import com.sound_bind.pay_service.domain.application.dto.request.SimplePaymentDetails
import com.sound_bind.pay_service.domain.application.manager.ChargeManager
import com.sound_bind.pay_service.domain.application.manager.impl.KafkaManagerImpl.Companion.PAY_SERVICE_GROUP
import com.sound_bind.pay_service.domain.application.manager.impl.KafkaManagerImpl.Companion.USER_ADDED_TOPIC
import com.sound_bind.pay_service.domain.persistence.entity.ChargeType
import com.sound_bind.pay_service.domain.persistence.entity.Point
import com.sound_bind.pay_service.domain.persistence.repository.PointChargeRepository
import com.sound_bind.pay_service.domain.persistence.repository.PointRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PointManagementService(
    private val pointRepository: PointRepository,
    private val pointChargeRepository: PointChargeRepository,
    private val chargeManager: ChargeManager
) {

    @Transactional
    @KafkaListener(groupId = PAY_SERVICE_GROUP, topics = [USER_ADDED_TOPIC])
    fun createPointWithZero(@Payload payload: UserSignUpMessageDTO): Long {
        val point = Point(payload.userId)
        pointRepository.save(point)
        return point.id!!
    }

    @Transactional
    fun chargePoint(userId: Long, dto: PointChargeRequestDTO): Long {
        val point = pointRepository.findPointByUserId(userId)
            ?: throw IllegalArgumentException("Point with user id $userId doesn't exist")
        val chargeType = ChargeType.of(dto.chargeTypeDescription)
        val paymentDetails = dto.paymentDetails

        val chargeStrategy = when (chargeType) {
            ChargeType.CREDIT_CARD -> {
                val creditCardDetails = paymentDetails as CreditCardDetails
                CreditCardChargeStrategy(
                    creditCardDetails.cardNumber,
                    creditCardDetails.cardExpirationDate
                )
            }
            ChargeType.BANK_ACCOUNT -> {
                val bankAccountDetails = paymentDetails as BankAccountDetails
                BankAccountChargeStrategy(
                    bankAccountDetails.bank,
                    bankAccountDetails.accountNumber
                )
            }
            ChargeType.SIMPLE_PAYMENT -> {
                val simplePaymentDetails = paymentDetails as SimplePaymentDetails
                SimplePaymentChargeStrategy(
                    simplePaymentDetails.email,
                    simplePaymentDetails.phoneNumber
                )
            }
        }
        chargeManager.registerStrategy(chargeStrategy)
        val pointCharge = chargeManager.chargePoint(userId, point, dto)
        pointChargeRepository.save(pointCharge)

        return pointCharge.id!!
    }
}