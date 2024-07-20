package com.auth_service.domain.persistence.repository

import com.auth_service.domain.persistence.entity.UserProfile
import org.springframework.data.jpa.repository.JpaRepository

interface UserProfileRepository: JpaRepository<UserProfile, Long>