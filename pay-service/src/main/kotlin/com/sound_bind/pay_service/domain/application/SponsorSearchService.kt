package com.sound_bind.pay_service.domain.application

import com.sound_bind.pay_service.domain.persistence.entity.Sponsor
import com.sound_bind.pay_service.domain.persistence.es.document.SponsorDocument
import com.sound_bind.pay_service.domain.persistence.es.search.SponsorSearchRepository
import com.sound_bind.pay_service.global.exception.PayServiceException
import com.sound_bind.pay_service.global.util.DateTimeUtils
import org.springframework.stereotype.Service

@Service
class SponsorSearchService(
    private val sponsorSearchRepository: SponsorSearchRepository
) {

    fun saveSponsorOnElasticsearch(sponsor: Sponsor, pointId: Long) {
        val sponsorDocument = SponsorDocument(
            sponsor.id!!,
            sponsor.senderId,
            sponsor.receiverId,
            pointId,
            sponsor.pointAmount,
            sponsor.isCompleted,
            DateTimeUtils.convertLocalDateTimeToString(sponsor.createdAt)
        )
        sponsorSearchRepository.save(sponsorDocument)
    }

    fun updateSponsorReceivedOnElasticsearch(sponsorId: Long) {
        val sponsor = sponsorSearchRepository.findById(sponsorId)
            .orElseThrow { PayServiceException.SponsorNotFoundException("Sponsor not found") }
        sponsor.receive()
        sponsorSearchRepository.save(sponsor)
    }

    fun lookUpSponsorHistory(receiverId: Long): List<SponsorDocument> =
        sponsorSearchRepository.findAllByReceiverId(receiverId)
}