package com.auth_service.global.auth.oauth

import com.auth_service.global.auth.jwt.JwtProvider
import com.auth_service.global.auth.oauth.OAuth2AuthorizationRequestCookieRepository.Companion.REDIRECT_URI_PARAM_COOKIE_NAME
import com.auth_service.global.dto.response.TokenResponseDTO
import com.auth_service.global.util.CookieUtils
import com.auth_service.global.util.RedisUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI
import java.time.Duration

@Component
class OAuth2AuthenticationSuccessHandler(
    private val jwtProvider: JwtProvider,
    private val authorizationCookieRepository: OAuth2AuthorizationRequestCookieRepository
): SimpleUrlAuthenticationSuccessHandler() {

    private val log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler::class.java)

    @Value("\${authorized-redirect-uris}")
    private lateinit var authorizedRedirectUris: List<String>

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val targetUrl = determineTargetUrl(request, response, authentication)
        log.info("[Target URL] $targetUrl")

        if (response.isCommitted) {
            log.debug("Response has already been committed. Unable to redirect to $targetUrl")
            return
        }

        clearAuthenticationAttributes(request)
        authorizationCookieRepository.removeAuthorizationRequest(request, response)
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    override fun determineTargetUrl(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ): String {
        val redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)?.let { cookie ->
            val uri = cookie.value
            require(isAuthorizedRedirectUri(uri)) { "Unauthorized Redirect URI: $uri" }
            uri
        } ?: defaultTargetUrl

        val tokenResponseDTO = jwtProvider.generateToken(authentication)
        saveRefreshTokenOnRedis(tokenResponseDTO)

        return ServletUriComponentsBuilder
            .fromUriString(redirectUri)
            .queryParam("token", tokenResponseDTO.accessToken)
            .queryParam("error", "")
            .toUriString()
    }

    private fun isAuthorizedRedirectUri(uri: String): Boolean =
        authorizedRedirectUris.any { authorizedRedirectUri ->
            val clientRedirectURI = URI.create(uri)
            URI.create(authorizedRedirectUri).let {
                it.host.equals(clientRedirectURI.host, ignoreCase = true) &&
                        it.port == clientRedirectURI.port
            }
        }

    private fun saveRefreshTokenOnRedis(tokenResponseDTO: TokenResponseDTO) =
        RedisUtils.saveValue(
            tokenResponseDTO.accessToken,
            tokenResponseDTO.refreshToken,
            Duration.ofMillis(tokenResponseDTO.refreshTokenValidTime)
        )
}