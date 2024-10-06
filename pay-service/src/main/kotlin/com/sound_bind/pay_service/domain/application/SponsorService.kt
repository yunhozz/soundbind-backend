package com.sound_bind.pay_service.domain.application

import com.sound_bind.pay_service.domain.application.dto.request.SponsorRequestDTO
import com.sound_bind.pay_service.domain.application.manager.ElasticsearchManager
import com.sound_bind.pay_service.domain.application.manager.KafkaManager
import com.sound_bind.pay_service.domain.persistence.entity.Sponsor
import com.sound_bind.pay_service.domain.persistence.repository.PointRepository
import com.sound_bind.pay_service.domain.persistence.repository.SponsorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SponsorService(
    private val pointRepository: PointRepository,
    private val sponsorRepository: SponsorRepository,
    private val kafkaManager: KafkaManager,
    private val elasticsearchManager: ElasticsearchManager
) {

    @Transactional
    fun sendSponsorAndSubtractPoint(senderId: Long, dto: SponsorRequestDTO): Long {
        val senderPoint = pointRepository.findByUserId(senderId)
            ?: throw IllegalArgumentException("Point with user id $senderId doesn't exist")
        val receiverId = dto.receiverId
        val pointAmount = dto.pointAmount

        val sponsor = Sponsor.createAndSubtractPoint(senderId, receiverId, senderPoint, pointAmount)
        kafkaManager.sendSponsorReceivedTopic(
            receiverId,
            content = "Tester 님이 당신에게 $pointAmount 포인트를 후원하셨습니다.",
            link = null
        )
        sponsorRepository.save(sponsor)

        elasticsearchManager.saveSponsorDocument(sponsor, senderPoint.id!!)

        return sponsor.id!!
    }

    @Transactional
    fun receiveSponsorAndAddPoint(receiverId: Long, sponsorId: Long) {
        val sponsor = sponsorRepository.findByIdAndReceiverId(sponsorId, receiverId)
            ?: throw IllegalArgumentException("Sponsor with user id $receiverId doesn't exist")
        sponsor.receive()
        elasticsearchManager.updateSponsorReceived(sponsor.id!!)

        val receiverPoint = pointRepository.findByUserId(receiverId)
            ?: throw IllegalArgumentException("Point with user id $receiverId doesn't exist")
        receiverPoint.addAmount(sponsor.pointAmount)
    }
}