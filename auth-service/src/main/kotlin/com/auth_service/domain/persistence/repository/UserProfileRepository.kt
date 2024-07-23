package com.auth_service.domain.persistence.repository

import com.auth_service.domain.persistence.entity.User
import com.auth_service.domain.persistence.entity.UserProfile
import com.auth_service.global.exception.UserManageException.UserProfileNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserProfileRepository: JpaRepository<UserProfile, Long> {

    fun existsByEmail(email: String): Boolean

    fun findByUser(user: User): UserProfile?

    @Query("select up from UserProfile up join fetch up.user u where up.email = :email")
    fun findWithUserByEmail(email: String): UserProfile?

    fun deleteByUser(user: User) {
        val userProfile = findByUser(user)
            ?: throw UserProfileNotFoundException("User profile does not exist")
        delete(userProfile)
    }
}