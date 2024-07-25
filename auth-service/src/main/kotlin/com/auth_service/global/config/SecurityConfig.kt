package com.auth_service.global.config

import com.auth_service.global.auth.JwtAccessDeniedHandler
import com.auth_service.global.auth.JwtAuthenticationEntryPoint
import com.auth_service.global.auth.JwtFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtFilter: JwtFilter,
    private val jwtAccessDeniedHandler: JwtAccessDeniedHandler,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .cors { it.disable() }
            .csrf { it.disable() }
            .authorizeHttpRequests { it.anyRequest().permitAll() } //TODO
            .headers { it.frameOptions { fo -> fo.sameOrigin() } }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling {
                it.accessDeniedHandler(jwtAccessDeniedHandler)
                it.authenticationEntryPoint(jwtAuthenticationEntryPoint)
            }
            .build()

    @Bean
    fun bcryptPasswordEncoder() = BCryptPasswordEncoder()
}