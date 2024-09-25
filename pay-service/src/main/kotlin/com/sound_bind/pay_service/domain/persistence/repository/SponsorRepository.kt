package com.sound_bind.pay_service.domain.persistence.repository

import com.sound_bind.pay_service.domain.persistence.entity.Sponsor
import org.springframework.data.jpa.repository.JpaRepository

interface SponsorRepository: JpaRepository<Sponsor, Long>