package com.auth_service.domain.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne

@Entity
class UserPassword private constructor(
    @OneToOne(fetch = FetchType.LAZY)
    val user: User,
    password: String
): BaseEntity() {

    companion object {
        fun create(user: User, password: String) = UserPassword(user, password)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    var password = password
        protected set
}