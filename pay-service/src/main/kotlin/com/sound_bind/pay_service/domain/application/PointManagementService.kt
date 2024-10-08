package com.sound_bind.pay_service.domain.application

import com.sound_bind.pay_service.domain.application.dto.event.ClearPointCommitEvent
import com.sound_bind.pay_service.domain.application.dto.event.UserSignUpEvent
import com.sound_bind.pay_service.domain.application.dto.event.UserWithdrawEvent
import com.sound_bind.pay_service.domain.application.dto.request.PointChargeRequestDTO
import com.sound_bind.pay_service.domain.application.dto.response.PointResponseDTO
import com.sound_bind.pay_service.domain.application.manager.AsyncManager
import com.sound_bind.pay_service.domain.application.manager.ChargeManager
import com.sound_bind.pay_service.domain.application.manager.ElasticsearchManager
import com.sound_bind.pay_service.domain.application.manager.impl.KafkaManagerImpl.Companion.POINT_MANAGE_SERVICE_GROUP
import com.sound_bind.pay_service.domain.application.manager.impl.KafkaManagerImpl.Companion.USER_ADDED_TOPIC
import com.sound_bind.pay_service.domain.application.manager.impl.KafkaManagerImpl.Companion.USER_DELETION_TOPIC
import com.sound_bind.pay_service.domain.persistence.entity.Point
import com.sound_bind.pay_service.domain.persistence.repository.PointChargeRepository
import com.sound_bind.pay_service.domain.persistence.repository.PointRepository
import com.sound_bind.pay_service.global.exception.PayServiceException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PointManagementService(
    private val pointRepository: PointRepository,
    private val pointChargeRepository: PointChargeRepository,
    private val chargeManager: ChargeManager,
    private val elasticsearchManager: ElasticsearchManager,
    private val asyncManager: AsyncManager,
    private val eventPublisher: ApplicationEventPublisher
) {

    @Transactional
    @KafkaListener(groupId = POINT_MANAGE_SERVICE_GROUP, topics = [USER_ADDED_TOPIC])
    fun createPointWithZero(@Payload payload: UserSignUpEvent): Long {
        val point = Point(payload.userId)
        pointRepository.save(point)
        return point.id!!
    }

    @Transactional
    fun chargePoint(userId: Long, dto: PointChargeRequestDTO): Long {
        val point = findPointByUserId(userId)
        val pointCharge = chargeManager.chargePoint(userId, point, dto)

        pointChargeRepository.save(pointCharge)
        elasticsearchManager.savePointChargeDocument(pointCharge, point.id!!)

        return pointCharge.id!!
    }

    @Transactional(readOnly = true)
    fun lookUpMyPoint(userId: Long): PointResponseDTO {
        val point = findPointByUserId(userId)
        return PointResponseDTO(
            point.id!!,
            point.userId,
            point.amount
        )
    }

    /*
    1. User Withdraw
    2. Music List Delete
    3. Review & Comment List Delete
    4. Point & Point Charge List Delete
    5. Sponsor List Delete
     */

    @Transactional
    @KafkaListener(groupId = POINT_MANAGE_SERVICE_GROUP, topics = [USER_DELETION_TOPIC])
    fun clearPointByUserWithdraw(@Payload payload: UserWithdrawEvent) {
        val userId = payload.userId
        val point = findPointByUserId(userId)

        if (point.amount > 0) {
            // TODO : 포인트 존재 시 롤백 -> Kafka
//            throw IllegalArgumentException("There are remaining points. Please proceed with the refund first.")
        }

        val pointChargeList = pointChargeRepository.findAllByPoint(point)
        asyncManager.softDeletePointChargeList(pointChargeList)
        elasticsearchManager.deletePointChargeDocumentList(pointChargeList.map { it.id!! })

        eventPublisher.publishEvent(ClearPointCommitEvent(userId, point.id!!))
        point.softDelete()
    }

    private fun findPointByUserId(userId: Long): Point =
        pointRepository.findByUserId(userId)
            ?: throw PayServiceException.PointNotFoundException("Point with user id $userId doesn't exist")
}