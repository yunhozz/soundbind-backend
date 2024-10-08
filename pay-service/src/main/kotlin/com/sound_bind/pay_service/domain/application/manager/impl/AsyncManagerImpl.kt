package com.sound_bind.pay_service.domain.application.manager.impl

import com.sound_bind.pay_service.domain.application.manager.AsyncManager
import com.sound_bind.pay_service.domain.persistence.entity.PointCharge
import com.sound_bind.pay_service.domain.persistence.entity.Sponsor
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class AsyncManagerImpl: AsyncManager {

    @Async
    override fun softDeletePointChargeList(pointChargeList: List<PointCharge>) =
        pointChargeList.forEach { it.softDelete() }

    @Async
    override fun softDeleteSponsorList(sponsorList: List<Sponsor>) {
        sponsorList.forEach { it.softDelete() }
    }
}