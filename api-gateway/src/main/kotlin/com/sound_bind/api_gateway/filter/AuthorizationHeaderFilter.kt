package com.sound_bind.api_gateway.filter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.concurrent.atomic.AtomicInteger

@Component
class AuthorizationHeaderFilter
    : AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config>(Config::class.java) {

    private val log = LoggerFactory.getLogger(AuthorizationHeaderFilter::class.java)

    override fun apply(config: Config?): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val request = exchange.request
            val accessToken = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
                ?: throw RuntimeException("Token is Missing!!")

            log.info("[Request URI] ${request.uri}")

            addSubjectOnRequest(accessToken.split(" ")[1], exchange, chain)
        }
    }

    private fun addSubjectOnRequest(
        token: String,
        exchange: ServerWebExchange,
        chain: GatewayFilterChain
    ): Mono<Void> {
        val retryCount = AtomicInteger(0)
        return WebClient.create()
            .get()
            .uri("http://localhost:8090/api/auth/subject")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, token)
            .retrieve()
            .bodyToMono(String::class.java)
            .flatMap { subject ->
                val requestMutate = exchange.request.mutate()
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .header("sub", subject)
                    .build()

                val exchangeMutate = exchange.mutate()
                    .request(requestMutate)
                    .build()

                chain.filter(exchangeMutate)
            }
            .onErrorResume {
                log.error("[Error Message] ${it.localizedMessage}", it)
                val attempts = retryCount.incrementAndGet()

                attempts.takeIf { att -> att <= 3 }
                    ?.run { tokenRefreshRequest(exchange, chain, token) }
                    ?: Mono.error(it)
            }
    }

    private fun tokenRefreshRequest(exchange: ServerWebExchange, chain: GatewayFilterChain, token: String): Mono<Void> =
        WebClient.create()
            .get()
            .uri("http://localhost:8090/api/auth/token/refresh")
            .accept(MediaType.APPLICATION_JSON)
            .header("X-Token-Expired", token)
            .retrieve()
            .bodyToMono(String::class.java)
            .flatMap { response ->
                val obj = jacksonObjectMapper().readValue(response, Map::class.java)
                val accessToken = obj["accessToken"] as String
                addSubjectOnRequest(accessToken, exchange, chain)
            }

    class Config
}