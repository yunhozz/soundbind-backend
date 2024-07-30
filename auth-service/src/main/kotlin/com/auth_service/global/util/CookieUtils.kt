package com.auth_service.global.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.web.server.Cookie.SameSite
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.util.SerializationUtils
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.time.Duration
import java.util.Base64

class CookieUtils {

    companion object {
        fun getCookie(request: HttpServletRequest, name: String): Cookie =
            request.cookies?.find { it.name == name }
                ?: throw IllegalArgumentException("Cookie with name $name not found")

        fun addCookie(response: HttpServletResponse, name: String, value: String, maxAge: Long?) {
            val cookieBuilder = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite(SameSite.LAX.attributeValue())
            maxAge?.let { cookieBuilder.maxAge(Duration.ofMillis(it)) }
            val cookie = cookieBuilder.build()
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
        }

        fun deleteCookie(request: HttpServletRequest, response: HttpServletResponse, name: String) =
            request.cookies?.find { it.name == name }
                ?.apply {
                    value = ""
                    path = "/"
                    maxAge = 0
                    response.addCookie(this)
                } ?: throw IllegalArgumentException("Cookie with name $name not found")

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
}