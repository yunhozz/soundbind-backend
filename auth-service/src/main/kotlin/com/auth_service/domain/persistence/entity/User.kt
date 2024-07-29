package com.auth_service.domain.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class User private constructor(
    loginType: LoginType,
    role: Role
): BaseEntity() {

    companion object {
        fun createGuest() = User(LoginType.LOCAL, Role.GUEST)
        fun createWithTypes(loginType: LoginType, role: Role) = User(loginType, role)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Enumerated(EnumType.STRING)
    var loginType = loginType
        protected set

    @Enumerated(EnumType.STRING)
    var role = role
        protected set

    internal fun updateLoginType(loginType: LoginType) {
        this.loginType = loginType
    }

    enum class LoginType {
        LOCAL, SOCIAL
    }

    enum class Role {
        ADMIN, USER, GUEST;

        companion object {
            fun of(name: String): Role = entries.find {
                it.name == name
            } ?: throw IllegalArgumentException("Unknown role: $name")
        }
    }
}