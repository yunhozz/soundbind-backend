package com.auth_service.domain.persistence.repository

import com.auth_service.domain.persistence.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<User, Long>