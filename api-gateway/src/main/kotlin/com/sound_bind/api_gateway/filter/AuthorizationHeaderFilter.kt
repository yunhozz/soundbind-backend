package com.sound_bind.api_gateway.filter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.util.Base64

@Component
class AuthorizationHeaderFilter
    : AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config>(Config::class.java) {

    companion object {
        private const val USER_SUBJECT_INQUIRY_URI = "http://localhost:8090/api/auth/subject"
        private const val TOKEN_REFRESH_URI = "http://localhost:8090/api/auth/token/refresh"
    }

    private val log = LoggerFactory.getLogger(AuthorizationHeaderFilter::class.java)

    override fun apply(config: Config?) =
        OrderedGatewayFilter(
            { exchange, chain ->
                val request = exchange.request
                val response = exchange.response

                val cookie = request.cookies.getFirst("atk")
                val cookieValue = cookie?.value
                    ?: throw RuntimeException("Token is Missing!!")

                log.info("[Authorization Header Filter Start] Request ID -> ${request.id}")
                log.info("Request URI : ${request.uri}")

                val bytes = Base64.getUrlDecoder().decode(cookieValue)
                val token = ByteArrayInputStream(bytes).use { bais ->
                    ObjectInputStream(bais).use { ois ->
                        ois.readObject().toString()
                    }
                }

                addSubjectOnRequest(token, exchange, chain)
                    .then(Mono.fromRunnable {
                        log.info("[Authorization Header Filter End] Response Code -> ${response.statusCode}")
                    })
            }, -1
        )

    private fun addSubjectOnRequest(token: String, exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> =
        WebClient.create()
            .get()
            .uri(USER_SUBJECT_INQUIRY_URI)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, token)
            .retrieve()
            .bodyToMono(String::class.java)
            .flatMap { response ->
                val obj = jacksonObjectMapper().readValue(response, Map::class.java)
                val requestMutate = exchange.request.mutate()
                    .headers {
                        it.add(HttpHeaders.AUTHORIZATION, token)
                        it.add("sub", obj["subject"] as String)
                        it.add("role", obj["role"] as String)
                    }
                    .build()

                val exchangeMutate = exchange.mutate()
                    .request(requestMutate)
                    .build()

                chain.filter(exchangeMutate)
            }
            .onErrorResume {
                log.warn("Token Expired!!")
                tokenRefreshRequest(exchange, chain)
            }

    private fun tokenRefreshRequest(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val cookie = request.cookies.getFirst("atk")
            ?: return Mono.error(RuntimeException("Token is Missing!!"))

        return WebClient.create()
            .get()
            .uri(TOKEN_REFRESH_URI)
            .accept(MediaType.APPLICATION_JSON)
            .cookie("atk", cookie.value)
            .exchangeToMono { response ->
                val headers = response.headers().asHttpHeaders()
                val cookies = headers[HttpHeaders.SET_COOKIE]
                response.bodyToMono(String::class.java)
                    .flatMap {
                        val obj = jacksonObjectMapper().readValue(it, Map::class.java)
                        val accessToken = obj["accessToken"] as String
                        cookies?.forEach { cookie ->
                            val httpHeaders = exchange.response.headers
                            httpHeaders.add(HttpHeaders.SET_COOKIE, cookie)
                        }
                        addSubjectOnRequest(accessToken, exchange, chain)
                    }
                    .onErrorResume {
                        log.error("Error Message : ${it.localizedMessage}", it)
                        Mono.error(RuntimeException(it.localizedMessage))
                    }
            }
    }

    class Config
}