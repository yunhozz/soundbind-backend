package com.auth_service.global.auth.enums

import org.springframework.security.core.GrantedAuthority

enum class Role(val auth: String): GrantedAuthority {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER"),
    GUEST("ROLE_GUEST")
    ;

    override fun getAuthority() = auth

    companion object {
        fun of(name: String): Role = entries.find {
            it.name == name
        } ?: throw IllegalArgumentException("Unknown role: $name")
    }
}