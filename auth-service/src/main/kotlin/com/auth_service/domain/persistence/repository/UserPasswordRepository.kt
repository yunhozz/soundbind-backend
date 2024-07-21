package com.auth_service.domain.persistence.repository

import com.auth_service.domain.persistence.entity.UserPassword
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserPasswordRepository: JpaRepository<UserPassword, Long> {

    @Query("select up from UserPassword up join fetch up.user u where u.id = :userId")
    fun findWithUserByUserId(userId: Long): UserPassword?
}