package com.auth_service.domain.persistence.repository

import com.auth_service.domain.persistence.entity.User
import com.auth_service.domain.persistence.entity.UserPassword
import org.springframework.data.jpa.repository.JpaRepository

interface UserPasswordRepository: JpaRepository<UserPassword, Long> {

    fun findByUser(user: User): UserPassword?
}