package com.auth_service.domain.persistence.repository

import com.auth_service.domain.persistence.entity.User
import com.auth_service.domain.persistence.entity.UserProfile
import com.auth_service.domain.persistence.repository.dto.UserSimpleInfoQueryDTO
import com.auth_service.global.exception.UserManageException.UserProfileNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserProfileRepository: JpaRepository<UserProfile, Long> {

    fun existsByEmail(email: String): Boolean

    fun existsByNickname(nickname: String): Boolean

    fun findByUser(user: User): UserProfile?

    @Query(
        "select u.id as id, up.nickname as nickname, up.profileUrl as profileUrl " +
                "from UserProfile up " +
                "join up.user u " +
                "where up.email = :email"
    )
    fun findSimpleInfoByEmail(email: String): UserSimpleInfoQueryDTO?

    @Query("select up from UserProfile up join fetch up.user u where up.email = :email")
    fun findWithUserByEmail(email: String): UserProfile?

    fun deleteByUser(user: User) {
        val userProfile = findByUser(user)
            ?: throw UserProfileNotFoundException("User profile does not exist")
        delete(userProfile)
    }
}