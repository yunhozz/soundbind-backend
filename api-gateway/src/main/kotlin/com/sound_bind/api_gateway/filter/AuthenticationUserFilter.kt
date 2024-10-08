package com.sound_bind.api_gateway.filter

import com.sound_bind.api_gateway.handler.exception.BusinessException.NoPermissionException
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class AuthenticationUserFilter
    : AbstractGatewayFilterFactory<AuthenticationUserFilter.Config>(Config::class.java) {

    private val log = LoggerFactory.getLogger(AuthenticationUserFilter::class.java)

    override fun apply(config: Config?) =
        GatewayFilter { exchange, chain ->
            val request = exchange.request
            val response = exchange.response
            val role = request.headers.getFirst("role")

            log.info("[Authentication User Filter Start] Request ID -> ${request.id}")

            if (role != "USER") {
                return@GatewayFilter Mono.error(
                    NoPermissionException("You do not have USER permissions. role = $role")
                )
            }

            chain.filter(exchange)
                .then(Mono.fromRunnable {
                    log.info("[Authentication User Filter End] Response Code -> ${response.statusCode}")
                })
        }

    class Config
}