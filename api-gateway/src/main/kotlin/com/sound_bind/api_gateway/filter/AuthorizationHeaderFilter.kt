package com.sound_bind.api_gateway.filter

import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

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

            WebClient.create()
                .get()
                .uri("http://localhost:8090/api/auth/subject")
                .header(HttpHeaders.AUTHORIZATION, token.split(" ")[1])
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String::class.java)
                .flatMap { subject ->
                    log.info("[Subject] $subject")
                    val requestMutate = request.mutate()
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .header("sub", subject)
                        .build()
                    val exchangeMutate = exchange.mutate()
                        .request(requestMutate)
                        .build()

                    chain.filter(exchangeMutate)
                        .then(Mono.fromRunnable {
                            log.info("[Request Headers] ${exchangeMutate.request.headers}")
                        })
                }
        }
    }

    class Config
}