package com.auth_service.global.auth

import com.auth_service.domain.persistence.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserDetailsImpl(
    private val userId: Long,
    private val password: String,
    private val roles: Set<User.Role>
): UserDetails {

    override fun getUsername(): String = userId.toString()

    override fun getPassword(): String = password

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        val authorities = mutableSetOf<SimpleGrantedAuthority>()
        roles.forEach { role ->
            val simpleGrantedAuthority = SimpleGrantedAuthority(role.name)
            authorities.add(simpleGrantedAuthority)
        }
        return authorities
    }
}