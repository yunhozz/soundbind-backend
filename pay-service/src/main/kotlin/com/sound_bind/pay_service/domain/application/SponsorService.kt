package com.sound_bind.pay_service.domain.application

import com.sound_bind.global.utils.RedisUtils
import com.sound_bind.pay_service.domain.application.dto.event.ClearPointCommitEvent
import com.sound_bind.pay_service.domain.application.dto.request.SponsorRequestDTO
import com.sound_bind.pay_service.domain.application.manager.AsyncManager
import com.sound_bind.pay_service.domain.application.manager.ElasticsearchManager
import com.sound_bind.pay_service.domain.application.manager.KafkaManager
import com.sound_bind.pay_service.domain.persistence.entity.Sponsor
import com.sound_bind.pay_service.domain.persistence.repository.PointRepository
import com.sound_bind.pay_service.domain.persistence.repository.SponsorRepository
import com.sound_bind.pay_service.global.exception.PayServiceException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Service
class SponsorService(
    private val pointRepository: PointRepository,
    private val sponsorRepository: SponsorRepository,
    private val kafkaManager: KafkaManager,
    private val asyncManager: AsyncManager,
    private val elasticsearchManager: ElasticsearchManager,
    private val eventPublisher: ApplicationEventPublisher
) {

    @Transactional
    fun sendSponsorAndSubtractPoint(senderId: Long, dto: SponsorRequestDTO): Long {
        val senderPoint = pointRepository.findByUserId(senderId)
            ?: throw PayServiceException.PointNotFoundException("Point with user id $senderId doesn't exist")
        val receiverId = dto.receiverId
        val pointAmount = dto.pointAmount

        val sponsor = Sponsor.createAndSubtractPoint(senderId, receiverId, senderPoint, pointAmount)
        sponsorRepository.save(sponsor)
        elasticsearchManager.saveSponsorDocument(sponsor, senderPoint.id!!)

        val myInfo = RedisUtils.getJson("user:$senderId", Map::class.java)
        kafkaManager.sendSponsorReceivedTopic(
            receiverId,
            content = "${myInfo["nickname"]}님이 당신에게 $pointAmount 포인트를 후원하셨습니다.",
            link = null
        )

        return sponsor.id!!
    }

    @Transactional
    fun receiveSponsorAndAddPoint(receiverId: Long, sponsorId: Long) {
        val sponsor = sponsorRepository.findByIdAndReceiverId(sponsorId, receiverId)
            ?: throw PayServiceException.SponsorNotFoundException("Sponsor with user id $receiverId doesn't exist")
        sponsor.receive()
        elasticsearchManager.updateSponsorReceived(sponsor.id!!)

        val receiverPoint = pointRepository.findByUserId(receiverId)
            ?: throw PayServiceException.PointNotFoundException("Point with user id $receiverId doesn't exist")
        receiverPoint.addAmount(sponsor.pointAmount)
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun clearSponsorHistoryByUserWithdraw(event: ClearPointCommitEvent) {
        val userId = event.userId
        if (sponsorRepository.existsByReceiverIdAndIsCompletedIsFalse(userId)) {
            throw IllegalArgumentException("There is a sponsorship that has not yet been received.")
        }

        val sponsorList = sponsorRepository.findAllByReceiverId(userId)
        asyncManager.softDeleteSponsorList(sponsorList)
        elasticsearchManager.deleteSponsorDocumentList(sponsorList.map { it.id!! })
    }
}