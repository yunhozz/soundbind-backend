package com.auth_service.global.config

import com.auth_service.domain.application.OAuth2UserCustomService
import com.auth_service.global.auth.enums.Role
import com.auth_service.global.auth.jwt.JwtAccessDeniedHandler
import com.auth_service.global.auth.jwt.JwtAuthenticationEntryPoint
import com.auth_service.global.auth.jwt.JwtFilter
import com.auth_service.global.auth.oauth.OAuth2AuthenticationFailureHandler
import com.auth_service.global.auth.oauth.OAuth2AuthenticationSuccessHandler
import com.auth_service.global.auth.oauth.OAuth2AuthorizationRequestCookieRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
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
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
    private val oAuth2UserCustomService: OAuth2UserCustomService,
    private val oAuth2AuthenticationSuccessHandler: OAuth2AuthenticationSuccessHandler,
    private val oAuth2AuthenticationFailureHandler: OAuth2AuthenticationFailureHandler,
    private val oAuth2AuthorizationCookieRepository: OAuth2AuthorizationRequestCookieRepository
) {

    companion object {
        private const val OAUTH2_AUTHORIZATION_URI = "/oauth2/authorization"
        private const val OAUTH2_REDIRECT_URI = "/oauth2/callback/*"
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .cors { it.disable() }
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                it.requestMatchers(HttpMethod.DELETE, "/api/users").permitAll()
                it.requestMatchers("/api/auth/**").permitAll()
            }
            .headers { it.frameOptions { fo -> fo.sameOrigin() } }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling {
                it.accessDeniedHandler(jwtAccessDeniedHandler)
                it.authenticationEntryPoint(jwtAuthenticationEntryPoint)
            }
            .oauth2Login { ol ->
                ol.authorizationEndpoint { ep ->
                    ep.baseUri(OAUTH2_AUTHORIZATION_URI)
                    ep.authorizationRequestRepository(oAuth2AuthorizationCookieRepository)
                }
                ol.redirectionEndpoint { ep -> ep.baseUri(OAUTH2_REDIRECT_URI) }
                ol.userInfoEndpoint { ep -> ep.userService(oAuth2UserCustomService) }
                ol.successHandler(oAuth2AuthenticationSuccessHandler)
                ol.failureHandler(oAuth2AuthenticationFailureHandler)
            }
            .build()

    @Bean
    fun bcryptPasswordEncoder() = BCryptPasswordEncoder()
}