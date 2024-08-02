package com.sound_bind.api_gateway.filter

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class CustomGlobalFilter: GlobalFilter, Ordered {

    private val log = LoggerFactory.getLogger(GlobalFilter::class.java)

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val response = exchange.response
        val path = request.uri.path

        // Can't access the corresponding url
        if (RestrictedPath.isRestricted(path)) {
            response.statusCode = HttpStatus.NOT_FOUND
            return response.setComplete()
        }

        log.info("[Global Filter Start] Request ID -> ${request.id}")

        return chain.filter(exchange)
            .then(Mono.fromRunnable {
                log.info("[Global Filter End] Response Code -> ${response.statusCode}")
            })
    }

    override fun getOrder() = Ordered.HIGHEST_PRECEDENCE

    private enum class RestrictedPath(val pattern: Regex) {
        TOKEN_REFRESH(Regex("^/auth/token/refresh$")),
        GET_SUBJECT(Regex("^/auth/subject$")),
        GET_SIMPLE_USER_INFO(Regex("^/users/\\d+/simple$"))
        ;

        companion object {
            fun isRestricted(path: String): Boolean = entries.any { it.pattern.matches(path) }
        }
    }
}