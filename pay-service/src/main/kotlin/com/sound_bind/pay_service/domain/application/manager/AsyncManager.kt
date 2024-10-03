package com.sound_bind.pay_service.domain.application.manager

import com.sound_bind.pay_service.domain.persistence.entity.PointCharge

interface AsyncManager {
    fun savePointChargeDocumentOnElasticsearch(pointCharge: PointCharge, pointId: Long)
    fun softDeletePointChargeList(pointChargeList: List<PointCharge>)
}