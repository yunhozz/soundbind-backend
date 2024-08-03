package com.auth_service.domain.persistence.repository.dto

interface UserSimpleInfoQueryDTO {
    fun getNickname(): String
    fun getProfileUrl(): String
}