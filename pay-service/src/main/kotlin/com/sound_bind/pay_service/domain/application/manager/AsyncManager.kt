package com.sound_bind.pay_service.domain.application.manager

import com.sound_bind.pay_service.domain.persistence.entity.PointCharge
import com.sound_bind.pay_service.domain.persistence.entity.Sponsor

interface AsyncManager {
    fun softDeletePointChargeList(pointChargeList: List<PointCharge>)
    fun softDeleteSponsorList(sponsorList: List<Sponsor>)
}