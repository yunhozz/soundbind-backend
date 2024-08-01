package com.auth_service.global.auth.oauth

import com.auth_service.global.auth.oauth.OAuth2AuthorizationRequestCookieRepository.Companion.REDIRECT_URI_PARAM_COOKIE_NAME
import com.auth_service.global.util.CookieUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@Component
class OAuth2AuthenticationFailureHandler(
    private val authorizationCookieRepository: OAuth2AuthorizationRequestCookieRepository
): SimpleUrlAuthenticationFailureHandler() {

    private val log = LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler::class.java)

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        log.error(exception.localizedMessage)
        var redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)?.let { cookie ->
            cookie.value
        } ?: "/"

        redirectUri = ServletUriComponentsBuilder
            .fromUriString(redirectUri)
            .queryParam("token", "")
            .queryParam("error", exception.localizedMessage)
            .toUriString()

        authorizationCookieRepository.removeAuthorizationRequest(request, response)
        redirectStrategy.sendRedirect(request, response, redirectUri)
    }
}