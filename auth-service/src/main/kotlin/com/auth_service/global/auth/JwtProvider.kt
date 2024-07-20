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
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider: InitializingBean {

    companion object {
        private val log = LoggerFactory.getLogger(JwtProvider::class.java)
    }

    @Value("\${spring.jwt.tokenType}")
    private lateinit var tokenType: String

    @Value("\${spring.jwt.accessTokenValidTime}")
    private lateinit var accessTokenValidTime: String

    @Value("\${spring.jwt.refreshTokenValidTime}")
    private lateinit var refreshTokenValidTime: String

    private lateinit var secretKey: SecretKey

    override fun afterPropertiesSet() {
        secretKey = Jwts.SIG.HS256.key().build() // generate random secret key
    }

    fun generateToken(userId: Long, roles: Set<User.Role>): TokenResponseDTO {
        val claims = Jwts.claims()
            .subject(userId.toString())
            .build()
        val auth = roles.map { it.name }
            .joinToString { ", " }

        claims["auth"] = auth
        val accessToken = createToken(claims, TokenType.ACCESS, accessTokenValidTime.toLong())
        val refreshToken = createToken(claims, TokenType.REFRESH, refreshTokenValidTime.toLong())

        return TokenResponseDTO(accessToken, refreshToken)
    }

    fun getClaimsFromToken(token: String): Claims = parseToken(token).payload

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

    private fun createToken(claims: Claims, type: TokenType, validTime: Long): String {
        claims["type"] = type.name
        return Jwts.builder()
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