package com.auth_service.domain.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne

@Entity
class UserProfile(
    @OneToOne(fetch = FetchType.LAZY)
    val user: User,
    val email: String,
    name: String,
    nickname: String,
    profileUrl: String
): BaseEntity() {

    companion object {
        fun create(
            user: User,
            email: String,
            name: String,
            nickname: String,
            profileUrl: String
        ) = UserProfile(user, email, name, nickname, profileUrl)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    var name = name
        protected set

    var nickname = nickname
        protected set

    var profileUrl = profileUrl
        protected set
}