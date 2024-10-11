package com.sound_bind.global.annotation

import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class HeaderToken

@Component
class HeaderTokenResolver: HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(HeaderToken::class.java)
                && parameter.parameterType == String::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        return parameter.getParameterAnnotation(HeaderToken::class.java)?.run {
            webRequest.getNativeRequest(HttpServletRequest::class.java)?.let {
                return it.getHeader(HttpHeaders.AUTHORIZATION)
            }
        } ?: throw IllegalArgumentException("There is no JWT token on header")
    }
}