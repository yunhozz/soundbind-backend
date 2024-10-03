package com.sound_bind.pay_service.domain.application.manager.impl

import com.sound_bind.pay_service.domain.application.PointSearchService
import com.sound_bind.pay_service.domain.application.manager.AsyncManager
import com.sound_bind.pay_service.domain.persistence.entity.PointCharge
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class AsyncManagerImpl(
    private val pointSearchService: PointSearchService
): AsyncManager {

    @Async
    override fun savePointChargeDocumentOnElasticsearch(pointCharge: PointCharge, pointId: Long) =
        pointSearchService.savePointChargeOnElasticsearch(pointCharge, pointId)

    @Async
    override fun softDeletePointChargeList(pointChargeList: List<PointCharge>) =
        pointChargeList.forEach { it.softDelete() }
}