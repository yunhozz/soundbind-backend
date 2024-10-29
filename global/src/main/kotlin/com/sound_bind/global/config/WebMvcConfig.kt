package com.sound_bind.global.config

import com.sound_bind.global.annotation.HeaderSubjectResolver
import com.sound_bind.global.annotation.HeaderTokenResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    private val headerSubjectResolver: HeaderSubjectResolver,
    private val headerTokenResolver: HeaderTokenResolver
): WebMvcConfigurer {

    companion object {
        private const val MAX_AGE_SECS = 3600L
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowCredentials(true)
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
            .allowedHeaders("*")
            .maxAge(MAX_AGE_SECS)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.addAll(listOf(headerSubjectResolver, headerTokenResolver))
    }
}