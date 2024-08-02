package com.auth_service.global.auth.oauth

import com.auth_service.global.util.CookieUtils
import com.auth_service.global.util.CookieUtils.Companion.COOKIE_EXPIRE_SECONDS
import com.auth_service.global.util.CookieUtils.Companion.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME
import com.auth_service.global.util.CookieUtils.Companion.REDIRECT_URI_PARAM_COOKIE_NAME
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component

@Component
class OAuth2AuthorizationRequestCookieRepository: AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        authorizationRequest?.let { authRequest ->
            CookieUtils.addCookie(
                response,
                OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                CookieUtils.serialize(authRequest),
                COOKIE_EXPIRE_SECONDS
            )
            request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME)?.let { redirectUriAfterLogin ->
                CookieUtils.addCookie(
                    response,
                    REDIRECT_URI_PARAM_COOKIE_NAME,
                    redirectUriAfterLogin,
                    COOKIE_EXPIRE_SECONDS
                )
            } ?: throw RuntimeException("Redirect URI after Login is Missing")
        } ?: run {
            removeAuthorizationRequest(request, response)
        }
    }

    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? =
        CookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)?.let { cookie ->
            CookieUtils.deserialize(cookie, OAuth2AuthorizationRequest::class.java)
        }

    override fun removeAuthorizationRequest(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): OAuth2AuthorizationRequest? {
        CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
        CookieUtils.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME)
        return this.loadAuthorizationRequest(request)
    }
}