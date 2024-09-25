package com.sound_bind.pay_service.domain.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Point(
    val userId: Long
): BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var amount: Int = 0
        protected set

    fun addAmount(amount: Int) {
        this.amount += amount
    }

    fun subtractAmount(amount: Int) {
        val remain = this.amount - amount
        require(remain < 0) { "Amount can't be less than $amount" }
        this.amount = remain
    }
}