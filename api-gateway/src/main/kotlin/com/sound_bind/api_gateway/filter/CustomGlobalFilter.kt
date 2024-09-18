package com.sound_bind.api_gateway.filter

import com.sound_bind.api_gateway.handler.exception.BusinessException.InvalidApproachException
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CustomGlobalFilter: GlobalFilter {

    private val log = LoggerFactory.getLogger(GlobalFilter::class.java)

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val response = exchange.response

        log.info("[Global Filter Start] Request ID -> ${request.id}")
        log.info("Request URI : ${request.uri}")

        val path = request.uri.path

        // Can't access the corresponding url
        if (RestrictedPath.contains(path)) {
            return Mono.error(InvalidApproachException("That's the wrong approach"))
        }

        return chain.filter(exchange)
            .then(Mono.fromRunnable {
                log.info("[Global Filter End] Response Code -> ${response.statusCode}")
            })
    }

    private enum class RestrictedPath(val pattern: Regex) {
        TOKEN_REFRESH(Regex("^/auth/token/refresh$")),
        GET_SUBJECT(Regex("^/auth/subject$")),
        GET_SIMPLE_USER_INFO(Regex("^/users/\\d+/simple$"))
        ;

        companion object {
            fun contains(path: String): Boolean = entries.any { it.pattern.matches(path) }
        }
    }
}