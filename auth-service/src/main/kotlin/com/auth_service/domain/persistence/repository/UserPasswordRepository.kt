package com.auth_service.domain.persistence.repository

import com.auth_service.domain.persistence.entity.User
import com.auth_service.domain.persistence.entity.UserPassword
import com.auth_service.global.exception.UserManageException.UserPasswordNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserPasswordRepository: JpaRepository<UserPassword, Long> {

    fun findByUser(user: User): UserPassword?

    @Query("select up from UserPassword up join fetch up.user u where u.id = :userId")
    fun findWithUserByUserId(userId: Long): UserPassword?
    
    fun deleteByUser(user: User) {
        val userPassword = findByUser(user)
            ?: throw UserPasswordNotFoundException("User password does not exist.")
        delete(userPassword)
    }
}