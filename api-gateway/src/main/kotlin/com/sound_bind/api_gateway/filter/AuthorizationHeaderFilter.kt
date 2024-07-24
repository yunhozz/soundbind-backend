package com.sound_bind.api_gateway.filter

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component

@Component
class AuthorizationHeaderFilter
    : AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config>(Config::class.java) {

    private val log = LoggerFactory.getLogger(AuthorizationHeaderFilter::class.java)

    override fun apply(config: Config?): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val request = exchange.request
            val token = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
                ?: throw RuntimeException("Token is Missing!!")

            log.info("[Access Token] $token")
            log.info("[Request URI] ${request.uri}")

            val requestWithToken = request.mutate()
                .header(HttpHeaders.AUTHORIZATION, token)
                .build()

            chain.filter(
                exchange.mutate()
                    .request(requestWithToken)
                    .build()
            )
        }
    }

    class Config
}