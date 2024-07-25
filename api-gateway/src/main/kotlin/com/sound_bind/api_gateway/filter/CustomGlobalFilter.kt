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
            response.statusCode = HttpStatus.FORBIDDEN
            return response.setComplete()
        }

        log.info("[Global Filter Start] Request ID -> ${request.id}")

        return chain.filter(exchange)
            .then(Mono.fromRunnable {
                log.info("[Global Filter End] Response Code -> ${response.statusCode}")
            })
    }

    override fun getOrder() = -1

    private enum class RestrictedPath(val path: String) {
        TOKEN_REFRESH("/auth/token/refresh"),
        GET_SUBJECT("/auth/subject")
        ;

        companion object {
            fun isRestricted(path: String): Boolean = entries.any { path.contains(it.path) }
        }
    }
}