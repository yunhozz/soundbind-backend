package com.auth_service.domain.persistence.entity

import com.auth_service.global.auth.enums.LoginType
import com.auth_service.global.auth.enums.Role
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

    fun verify() {
        require(role != Role.GUEST) { "Role is not GUEST. Current role: $role" }
        role = Role.USER
    }

    internal fun updateLoginType(loginType: LoginType) {
        this.loginType = loginType
    }
}