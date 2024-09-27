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
    val originalAmount: Int,
    val pointAmount: Int
): BaseEntity() {

    companion object {
        fun createAndAddPoint(userId: Long, point: Point, chargeType: ChargeType, originalAmount: Int, pointAmount: Int): PointCharge {
            val pointCharge = PointCharge(userId, point, chargeType, originalAmount, pointAmount)
            point.addAmount(pointAmount)
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

enum class ChargeType(
    val type: String,
    val description: String
) {
    CREDIT_CARD("credit_card","신용카드"),
    BANK_ACCOUNT("bank_account", "은행 계좌"),
    SIMPLE_PAYMENT("simple_payment", "간편 결제")
    ;

    companion object {
        fun of(description: String): ChargeType = entries.find { it.description == description }
            ?: throw IllegalArgumentException("Unknown charge type: $description")

        fun of(chargeType: ChargeType): String = chargeType.type
    }
}