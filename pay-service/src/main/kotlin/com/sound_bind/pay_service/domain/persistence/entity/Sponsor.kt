package com.sound_bind.pay_service.domain.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

@Entity
@SQLRestriction("deleted_at is null")
class Sponsor private constructor(
    val senderId: Long,
    val receiverId: Long,
    @ManyToOne(fetch = FetchType.LAZY)
    val point: Point,
    val pointAmount: Int
): BaseEntity() {

    companion object {
        fun createAndSubtractPoint(senderId: Long, receiverId: Long, point: Point, pointAmount: Int): Sponsor {
            require(pointAmount > 0) { "Sponsorship is available from at least 1 point." }
            val sponsor = Sponsor(senderId, receiverId, point, pointAmount)
            point.subtractAmount(pointAmount)
            return sponsor
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var isCompleted: Boolean = false
        protected set

    private var deletedAt: LocalDateTime? = null

    fun receiveSponsor() {
        require(!isCompleted) { "Already Received this Sponsor!" }
        isCompleted = true
    }

    fun softDelete() {
        deletedAt ?: run { deletedAt = LocalDateTime.now() }
    }
}