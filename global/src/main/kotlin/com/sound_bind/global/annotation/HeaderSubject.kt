package com.sound_bind.global.annotation

import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class HeaderSubject

@Component
class HeaderSubjectResolver: HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(HeaderSubject::class.java)
                && parameter.parameterType == String::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        return parameter.getParameterAnnotation(HeaderSubject::class.java)?.run {
            webRequest.getNativeRequest(HttpServletRequest::class.java)?.let {
                return@run it.getHeader("sub")
            }
        } ?: throw IllegalArgumentException("There is no subject on header")
    }
}