package com.auth_service.global.auth.jwt

import io.jsonwebtoken.lang.Strings
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtFilter(private val jwtProvider: JwtProvider): OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(JwtFilter::class.java)

    @Value("\${jwt.tokenType}")
    private lateinit var tokenType: String

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestURI = request.requestURI
        val token = request.getHeader(HttpHeaders.AUTHORIZATION)
        log.info("[Request URI] $requestURI")

        resolveToken(token)?.let {
            if (jwtProvider.verifyToken(it)) {
                val authentication = jwtProvider.getAuthentication(token)
                SecurityContextHolder.getContext().authentication = authentication
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(token: String?): String? =
        token.takeIf { Strings.hasText(it) }?.let { resolveTokenParts(it) }

    private fun resolveTokenParts(token: String): String? {
        val parts = token.split(" ")
        return (parts.size == 2).and(parts[0] == tokenType).run {
            if (this) parts[1] else null
        }
    }
}