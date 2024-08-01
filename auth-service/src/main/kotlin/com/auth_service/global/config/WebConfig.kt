package com.auth_service.global.config

import com.auth_service.global.annotation.HeaderSubjectResolver
import com.auth_service.global.annotation.HeaderTokenResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig: WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(HeaderTokenResolver())
        resolvers.add(HeaderSubjectResolver())
    }
}