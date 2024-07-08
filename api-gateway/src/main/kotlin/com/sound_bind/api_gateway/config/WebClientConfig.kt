package com.sound_bind.api_gateway.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.ExchangeFunctions
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

@Configuration
class WebClientConfig {

    companion object {
        private val OM = ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(JavaTimeModule())

        private const val COMMON_WEB_CLIENT = "commonWebClient"
        private const val WEB_CLIENT_DEFAULT_HTTP_CLIENT = "defaultHttpClient"
        private const val WEB_CLIENT_CONNECTION_PROVIDER = "connectionProvider"
        private const val WEB_CLIENT_DEFAULT_EXCHANGE_STRATEGIES = "defaultExchangeStrategies"
    }

    @Bean(COMMON_WEB_CLIENT)
    fun commonWebClient(
        @Qualifier(WEB_CLIENT_DEFAULT_HTTP_CLIENT) httpClient: HttpClient,
        @Qualifier(WEB_CLIENT_DEFAULT_EXCHANGE_STRATEGIES) exchangeStrategies: ExchangeStrategies
    ) = WebClient.builder()
        .clientConnector(ReactorClientHttpConnector(HttpClient.create()))
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        .exchangeFunction(ExchangeFunctions.create(ReactorClientHttpConnector(httpClient), exchangeStrategies))
        .exchangeStrategies(exchangeStrategies)
        .build()

    @Bean(WEB_CLIENT_DEFAULT_HTTP_CLIENT)
    fun defaultWebClient(
        @Qualifier(WEB_CLIENT_CONNECTION_PROVIDER) provider: ConnectionProvider
    ) = HttpClient.create(provider)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
        .doOnConnected {
            it.addHandlerLast(ReadTimeoutHandler(5))
                .addHandlerLast(WriteTimeoutHandler(5))
        } // Read and write timeout (5 seconds)


    @Bean(WEB_CLIENT_CONNECTION_PROVIDER)
    fun connectionProvider() = ConnectionProvider.builder("http-pool")
        .maxConnections(100) // Number of connection pools
        .pendingAcquireTimeout(Duration.ofMillis(0)) // Maximum time to wait to get a connection from a connection pool
        .pendingAcquireMaxCount(-1) // Number of attempts to get a connection from the connection pool (-1: no limit)
        .maxIdleTime(Duration.ofMillis(2000L)) // Time to maintain connection in idle state in connection pool
        .build()

    @Bean(WEB_CLIENT_DEFAULT_EXCHANGE_STRATEGIES)
    fun defaultExchangeStrategies() = ExchangeStrategies.builder()
        .codecs {
            it.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(OM, MediaType.APPLICATION_JSON))
            it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(OM, MediaType.APPLICATION_JSON))
            it.defaultCodecs().maxInMemorySize(1024 * 1024) // Max buffer = 1MB
        }.build()
}