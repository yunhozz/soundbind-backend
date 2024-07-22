package com.auth_service.global.auth

import com.auth_service.domain.persistence.entity.User
import com.auth_service.global.dto.response.TokenResponseDTO
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider: InitializingBean {

    companion object {
        private val log = LoggerFactory.getLogger(JwtProvider::class.java)
    }

    @Value("\${jwt.tokenType}")
    private lateinit var tokenType: String

    @Value("\${jwt.accessTokenValidTime}")
    private lateinit var accessTokenValidTime: String

    @Value("\${jwt.refreshTokenValidTime}")
    private lateinit var refreshTokenValidTime: String

    private lateinit var secretKey: SecretKey

    override fun afterPropertiesSet() {
        secretKey = Jwts.SIG.HS256.key().build() // generate random secret key
    }

    fun generateToken(username: String, role: User.Role): TokenResponseDTO {
        val accessToken = createToken(username, role, TokenType.ACCESS, accessTokenValidTime.toLong())
        val refreshToken = createToken(username, role, TokenType.REFRESH, refreshTokenValidTime.toLong())
        return TokenResponseDTO(
            tokenType,
            accessToken,
            refreshToken,
            accessTokenValidTime.toLong(),
            refreshTokenValidTime.toLong()
        )
    }

    fun generateToken(authentication: Authentication): TokenResponseDTO {
        val authorities = authentication.authorities
        val authority = authorities.first().authority
        return generateToken(authentication.name, User.Role.of(authority))
    }

    fun getAuthentication(token: String): Authentication {
        val claims = parseToken(token).payload
        val username = claims.subject
        val role = claims["role"].toString()
        return UsernamePasswordAuthenticationToken(
            username,
            "",
            AuthorityUtils.createAuthorityList(role)
        )
    }

    fun verifyToken(token: String): Boolean {
        try {
            parseToken(token)
            return true
        } catch (e: Exception) {
            when (e) {
                is ExpiredJwtException -> log.error("Expired JWT token exception", e)
                is SecurityException, is MalformedJwtException -> log.error("Malformed JWT token exception", e)
                is UnsupportedJwtException -> log.error("Unsupported JWT token exception", e)
                is IllegalArgumentException -> log.error("Invalid JWT token exception", e)
                else -> throw e
            }
        }
        return false
    }

    private fun createToken(username: String, role: User.Role, type: TokenType, validTime: Long): String {
        val claims = Jwts.claims()
            .subject(username)
            .add("role", role.name)
            .add("type", type.name)
            .build()

        return Jwts.builder()
            .header()
            .type("JWT")
            .and()
            .claims(claims)
            .issuedAt(Date())
            .expiration(Date(Date().time + validTime))
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    private fun parseToken(token: String): Jws<Claims> =
        Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)

    private enum class TokenType {
        ACCESS, REFRESH
    }
}