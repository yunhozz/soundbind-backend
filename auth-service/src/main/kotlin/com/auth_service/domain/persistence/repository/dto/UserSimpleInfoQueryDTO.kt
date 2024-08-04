package com.auth_service.domain.persistence.repository.dto

interface UserSimpleInfoQueryDTO {
    fun getId(): Long
    fun getNickname(): String
    fun getProfileUrl(): String
}