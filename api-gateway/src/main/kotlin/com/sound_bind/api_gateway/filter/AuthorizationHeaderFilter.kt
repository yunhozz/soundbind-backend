package com.sound_bind.api_gateway.filter

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sound_bind.api_gateway.config.WebClientConfig.Companion.COMMON_WEB_CLIENT
import com.sound_bind.api_gateway.config.WebClientConfig.Companion.SSE_WEB_CLIENT
import com.sound_bind.api_gateway.handler.exception.BusinessException.TokenNotFoundOnCookieException
import com.sound_bind.api_gateway.handler.exception.BusinessException.TokenRefreshFailException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
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
class AuthorizationHeaderFilter(
    @Qualifier(COMMON_WEB_CLIENT) private val commonWebClient: WebClient,
    @Qualifier(SSE_WEB_CLIENT) private val sseWebClient: WebClient
): AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config>(Config::class.java) {

    private val log = LoggerFactory.getLogger(AuthorizationHeaderFilter::class.java)

    override fun apply(config: Config?) =
        OrderedGatewayFilter(
            { exchange, chain ->
                val request = exchange.request
                val response = exchange.response

                log.info("[Authorization Header Filter Start] Request ID -> ${request.id}")
                log.info("Request URI : ${request.uri}")

                val cookie = request.cookies.getFirst("atk")
                val cookieValue = cookie?.value
                    ?: throw TokenNotFoundOnCookieException("Token is missing!! Need login.")

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

    private fun addSubjectOnRequest(token: String, exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val webClient =
            if (request.uri.toString() == sseSubscribeUri) sseWebClient
            else commonWebClient

        return webClient
            .get()
            .uri(userSubjectInquiryUri)
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, token)
            .retrieve()
            .bodyToMono(String::class.java)
            .flatMap { response ->
                val obj = mapper.readValue(response, Map::class.java)
                val requestMutate = request.mutate()
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
                log.debug("Token Expired!!")
                tokenRefreshRequest(exchange, chain)
            }
    }

    private fun tokenRefreshRequest(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val cookie = request.cookies.getFirst("atk")
            ?: return Mono.error(TokenNotFoundOnCookieException("Token is missing!! Need login."))

        return commonWebClient
            .get()
            .uri(tokenRefreshUri)
            .accept(MediaType.APPLICATION_JSON)
            .cookie("atk", cookie.value)
            .exchangeToMono { response ->
                val headers = response.headers().asHttpHeaders()
                val cookies = headers[HttpHeaders.SET_COOKIE]
                response.bodyToMono(String::class.java)
                    .flatMap {
                        val obj = mapper.readValue(it, Map::class.java)
                        obj["accessToken"]?.let { accessToken ->
                            cookies?.forEach { cookie ->
                                val httpHeaders = exchange.response.headers
                                httpHeaders.add(HttpHeaders.SET_COOKIE, cookie)
                            }
                            addSubjectOnRequest(accessToken.toString(), exchange, chain)
                        } ?: run {
                            val errMsg = obj["message"] as String
                            Mono.error(TokenRefreshFailException(errMsg))
                        }
                    }
            }
    }

    @Value("\${uris.auth-service-uri:http://localhost:8090}/api/auth/subject")
    private lateinit var userSubjectInquiryUri: String

    @Value("\${uris.auth-service-uri:http://localhost:8090}/api/auth/token/refresh")
    private lateinit var tokenRefreshUri: String

    @Value("\${uris.api-gateway-uri:http://localhost:8000}/api/notifications/subscribe")
    private lateinit var sseSubscribeUri: String

    companion object {
        private val mapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    class Config
}