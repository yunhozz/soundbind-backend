package com.sound_bind.global.utils

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.web.server.Cookie.SameSite
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.util.SerializationUtils
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.util.Base64

object CookieUtils {
    const val ACCESS_TOKEN_COOKIE_NAME = "atk"
    const val OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request"
    const val REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri"
    const val COOKIE_EXPIRE_SECONDS = 180L

    fun getCookie(request: HttpServletRequest, name: String): Cookie? =
        request.cookies?.find { it.name == name }

    fun addCookie(response: HttpServletResponse, name: String, value: String, maxAgeSec: Long?) {
        val cookieBuilder = ResponseCookie.from(name, value)
            .path("/")
            .httpOnly(true)
            .secure(true)
            .sameSite(SameSite.LAX.attributeValue())

        maxAgeSec?.let { cookieBuilder.maxAge(it) }
            ?: run { cookieBuilder.maxAge(60 * 60 * 24) } // 1 day

        response.addHeader(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString())
    }

    fun deleteCookie(request: HttpServletRequest, response: HttpServletResponse, name: String) =
        request.cookies?.find { it.name == name }
            ?.apply {
                value = null
                path = "/"
                maxAge = 0
                response.addCookie(this)
            } ?: throw IllegalArgumentException("Cookie with name $name not found")

    fun deleteAllCookies(request: HttpServletRequest, response: HttpServletResponse) {
        val cookieNameSet = setOf(
            ACCESS_TOKEN_COOKIE_NAME,
            OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
            REDIRECT_URI_PARAM_COOKIE_NAME
        )
        request.cookies.forEach { cookie ->
            val cookieName = cookie.name
            if (cookieName in cookieNameSet) {
                Cookie(cookieName, null).apply {
                    path = "/"
                    maxAge = 0
                    response.addCookie(this)
                }
            }
        }
    }

    fun serialize(obj: Any): String {
        val bytes = SerializationUtils.serialize(obj)
        return Base64.getUrlEncoder().encodeToString(bytes)
    }

    fun <T> deserialize(cookie: Cookie, clazz: Class<T>): T {
        val bytes = Base64.getUrlDecoder().decode(cookie.value)
        ByteArrayInputStream(bytes).use { bais ->
            ObjectInputStream(bais).use { ois ->
                return clazz.cast(ois.readObject())
            }
        }
    }
}