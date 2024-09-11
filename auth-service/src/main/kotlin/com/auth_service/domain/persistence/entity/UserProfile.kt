package com.auth_service.domain.persistence.entity

import com.auth_service.global.auth.enums.LoginType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne

@Entity
class UserProfile private constructor(
    @OneToOne(fetch = FetchType.LAZY)
    val user: User,
    @Column(unique = true)
    val email: String,
    name: String,
    nickname: String,
    profileUrl: String?
): BaseEntity() {

    companion object {
        fun create(
            user: User,
            email: String,
            name: String,
            nickname: String,
            profileUrl: String?
        ) = UserProfile(user, email, name, nickname, profileUrl)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    var name = name
        protected set

    @Column(unique = true)
    var nickname = nickname
        protected set

    var profileUrl = profileUrl
        protected set

    fun updateBySocialLogin(name: String, loginType: LoginType) {
        this.name = name
        user.updateLoginType(loginType)
    }
}