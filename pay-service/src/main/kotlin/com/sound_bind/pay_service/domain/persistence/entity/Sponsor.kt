package com.sound_bind.pay_service.domain.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

@Entity
@SQLRestriction("deleted_at is null")
class Sponsor(
    val senderId: Long,
    val receiverId: Long,
    @OneToOne(fetch = FetchType.LAZY)
    val point: Point,
    val amount: Int
): BaseEntity() {

    companion object {
        fun createAndSubtractPoint(senderId: Long, receiverId: Long, point: Point, amount: Int): Sponsor {
            val sponsor = Sponsor(senderId, receiverId, point, amount)
            point.subtractAmount(amount)
            return sponsor
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

enum class ExchangeType {
    PAYMENT, SEND
}