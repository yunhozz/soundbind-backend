package com.sound_bind.pay_service.domain.persistence.repository

import com.sound_bind.pay_service.domain.persistence.entity.Sponsor
import org.springframework.data.jpa.repository.JpaRepository

interface SponsorRepository: JpaRepository<Sponsor, Long> {
    fun findAllByReceiverId(receiverId: Long): List<Sponsor>
    fun findByIdAndReceiverId(id: Long, receiverId: Long): Sponsor?
    fun existsByReceiverIdAndIsCompletedIsFalse(receiverId: Long): Boolean
}