package com.sound_bind.pay_service.domain.application

import com.sound_bind.pay_service.domain.persistence.entity.ChargeType
import com.sound_bind.pay_service.domain.persistence.entity.PointCharge
import com.sound_bind.pay_service.domain.persistence.es.document.PointChargeDocument
import com.sound_bind.pay_service.domain.persistence.es.search.PointChargeSearchRepository
import org.springframework.stereotype.Service

@Service
class PointSearchService(
    private val pointChargeSearchRepository: PointChargeSearchRepository
) {

    fun savePointChargeOnElasticsearch(pointCharge: PointCharge, pointId: Long) {
        val pointChargeDocument = PointChargeDocument(
            pointCharge.id!!,
            pointCharge.userId,
            pointId,
            ChargeType.of(pointCharge.chargeType),
            pointCharge.originalAmount,
            pointCharge.pointAmount
        )
        pointChargeSearchRepository.save(pointChargeDocument)
    }

    fun lookUpPointChargeHistory(userId: Long) =
        pointChargeSearchRepository.findAllByUserId(userId)

    fun deletePointChargeDocumentList(pointChargeIds: List<Long>) =
        pointChargeSearchRepository.deleteAllById(pointChargeIds)
}