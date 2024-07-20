package com.auth_service.domain.persistence.entity

import jakarta.persistence.CollectionTable
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn

@Entity
class User(
    loginType: LoginType = LoginType.LOCAL,
    roles: MutableSet<Role> = mutableSetOf(Role.GUEST)
): BaseEntity() {

    companion object {
        fun createGuest() = User()
        fun createWithTypes(loginType: LoginType, roles: MutableSet<Role>) = User(loginType, roles)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Enumerated(EnumType.STRING)
    var loginType = loginType
        protected set

    @ElementCollection(fetch = FetchType.LAZY, targetClass = Role::class)
    @CollectionTable(joinColumns = [JoinColumn(name = "user_id")], name = "user_role")
    @Enumerated(EnumType.STRING)
    var roles = roles

    fun addRoles(vararg role: Role) = roles.addAll(role)

    fun subtractRoles(vararg role: Role) = roles.removeIf { it.name in role.map { r -> r.name } }

    enum class LoginType {
        GOOGLE, KAKAO, NAVER, LOCAL
    }

    enum class Role {
        ADMIN, USER, GUEST
    }
}