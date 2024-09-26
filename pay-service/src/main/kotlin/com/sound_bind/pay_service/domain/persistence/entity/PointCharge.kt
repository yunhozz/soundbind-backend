package com.sound_bind.pay_service.domain.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

@Entity
@SQLRestriction("deleted_at is null")
class PointCharge private constructor(
    val userId: Long,
    @ManyToOne(fetch = FetchType.LAZY)
    val point: Point,
    @Enumerated(EnumType.STRING)
    val chargeType: ChargeType,
    val amount: Int
): BaseEntity() {

    companion object {
        fun createAndAddPoint(userId: Long, point: Point, chargeType: ChargeType, amount: Int): PointCharge {
            val pointCharge = PointCharge(userId, point, chargeType, amount)
            point.addAmount(amount)
            return pointCharge
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    private var deletedAt: LocalDateTime? = null

    fun softDelete() {
        deletedAt ?: run { deletedAt = LocalDateTime.now() }
    }
}

enum class ChargeType {
    CARD, BANK_ACCOUNT, SIMPLE_PAYMENT
}