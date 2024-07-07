package com.sound_bind.api_gateway.filter

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class GlobalFilter: AbstractGatewayFilterFactory<GlobalFilter.Companion.Config>(Config::class.java) {

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val request = exchange.request
            val response = exchange.response

            config.preLogger.let {
                log.info("[Global Filter Start] Request ID -> {}", request.id)
            }

            chain.filter(exchange)
                .then(Mono.fromRunnable {
                    config.postLogger.let {
                        log.info("[Global Filter End] Response Code -> {}", response.statusCode)
                    }
                })
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(GlobalFilter::class.java)

        data class Config(
            val preLogger: Boolean,
            val postLogger: Boolean
        )
    }
}