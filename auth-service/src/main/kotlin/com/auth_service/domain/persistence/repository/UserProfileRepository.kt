package com.auth_service.domain.persistence.repository

import com.auth_service.domain.persistence.entity.UserProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserProfileRepository: JpaRepository<UserProfile, Long> {

    fun existsByEmail(email: String): Boolean

    @Query("select up from UserProfile up join fetch up.user u where up.email = :email")
    fun findWithUserByEmail(email: String): UserProfile?
}