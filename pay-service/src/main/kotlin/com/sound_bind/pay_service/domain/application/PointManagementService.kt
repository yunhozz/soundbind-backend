package com.sound_bind.pay_service.domain.application

import com.sound_bind.pay_service.domain.application.dto.message.UserSignUpMessageDTO
import com.sound_bind.pay_service.domain.application.dto.request.PointChargeRequestDTO
import com.sound_bind.pay_service.domain.application.manager.ChargeManager
import com.sound_bind.pay_service.domain.application.manager.impl.KafkaManagerImpl.Companion.PAY_SERVICE_GROUP
import com.sound_bind.pay_service.domain.application.manager.impl.KafkaManagerImpl.Companion.USER_ADDED_TOPIC
import com.sound_bind.pay_service.domain.application.manager.impl.KafkaManagerImpl.Companion.USER_DELETION_TOPIC
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

        chargeManager.registerStrategy(chargeType, paymentDetails)

        val pointCharge = chargeManager.chargePoint(userId, point, dto)
        pointChargeRepository.save(pointCharge)

        return pointCharge.id!!
    }
}