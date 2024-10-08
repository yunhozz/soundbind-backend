package com.sound_bind.pay_service.domain.application.manager

import com.sound_bind.pay_service.domain.persistence.entity.PointCharge
import com.sound_bind.pay_service.domain.persistence.entity.Sponsor

interface ElasticsearchManager {
    fun savePointChargeDocument(pointCharge: PointCharge, pointId: Long)
    fun saveSponsorDocument(sponsor: Sponsor, pointId: Long)
    fun updateSponsorReceived(sponsorId: Long)
    fun deletePointChargeDocumentList(pointChargeIds: List<Long>)
    fun deleteSponsorDocumentList(sponsorIds: List<Long>)
}