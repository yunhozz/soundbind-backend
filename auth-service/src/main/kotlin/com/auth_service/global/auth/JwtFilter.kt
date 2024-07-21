package com.auth_service.global.auth

import io.jsonwebtoken.lang.Strings
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtFilter(private val jwtProvider: JwtProvider): OncePerRequestFilter() {

    companion object {
        private val log = LoggerFactory.getLogger(JwtFilter::class.java)
        private val JWT_TOKEN_REFRESH_URI = "/api/auth/token/reissue"
    }

    @Value("\${spring.jwt.tokenType}")
    private lateinit var tokenType: String

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestURI = request.requestURI
        val token = request.getHeader(HttpHeaders.AUTHORIZATION)
        log.info("[Request URI] $requestURI")

        (requestURI != JWT_TOKEN_REFRESH_URI).run {
            resolveToken(token)?.also {
                if (jwtProvider.verifyToken(token)) {
                    val authentication = jwtProvider.getAuthentication(it)
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun resolveToken(token: String): String? {
        return Strings.hasText(token).run {
            if (this) resolveTokenParts(token) else null
        }
    }

    private fun resolveTokenParts(token: String): String? {
        val parts = token.split(" ")
        return (parts.size == 2).and(parts[0] == tokenType).run {
            if (this) parts[1] else null
        }
    }
}