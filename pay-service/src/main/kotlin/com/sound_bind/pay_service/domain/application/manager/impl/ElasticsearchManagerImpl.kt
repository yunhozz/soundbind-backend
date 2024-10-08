package com.sound_bind.pay_service.domain.application.manager.impl

import com.sound_bind.pay_service.domain.application.PointSearchService
import com.sound_bind.pay_service.domain.application.SponsorSearchService
import com.sound_bind.pay_service.domain.application.manager.ElasticsearchManager
import com.sound_bind.pay_service.domain.persistence.entity.PointCharge
import com.sound_bind.pay_service.domain.persistence.entity.Sponsor
import org.springframework.stereotype.Component

@Component
class ElasticsearchManagerImpl(
    private val pointSearchService: PointSearchService,
    private val sponsorSearchService: SponsorSearchService
): ElasticsearchManager {

    override fun savePointChargeDocument(pointCharge: PointCharge, pointId: Long) =
        pointSearchService.savePointChargeOnElasticsearch(pointCharge, pointId)

    override fun saveSponsorDocument(sponsor: Sponsor, pointId: Long) =
        sponsorSearchService.saveSponsorOnElasticsearch(sponsor, pointId)

    override fun updateSponsorReceived(sponsorId: Long) =
        sponsorSearchService.updateSponsorReceivedOnElasticsearch(sponsorId)

    override fun deletePointChargeDocumentList(pointChargeIds: List<Long>) =
        pointSearchService.deletePointChargeDocumentList(pointChargeIds)

    override fun deleteSponsorDocumentList(sponsorIds: List<Long>) {
        sponsorSearchService.deleteSponsorDocumentList(sponsorIds)
    }
}