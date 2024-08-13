package com.auth_service.domain.persistence.repository.dto

interface UserSimpleInfoQueryDTO {
    fun getUserId(): Long
    fun getEmail(): String
    fun getNickname(): String
    fun getProfileUrl(): String
}