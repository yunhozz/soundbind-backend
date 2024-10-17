package com.sound_bind.kafka_server.global.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

@Configuration
class WebClientConfig {

    @Value("\${service.endpoints.auth-service:localhost:8090}")
    private lateinit var authServiceUrl: String

    @Value("\${service.endpoints.music-service:localhost:8070}")
    private lateinit var musicServiceUrl: String

    @Bean(AUTH_SERVICE_WEB_CLIENT)
    fun authServiceWebClient(
        @Qualifier(CLIENT_HTTP_CONNECTOR) connector: ClientHttpConnector
    ) = WebClient.builder()
        .clientConnector(connector)
        .baseUrl(authServiceUrl)
        .build()

    @Bean(MUSIC_SERVICE_WEB_CLIENT)
    fun musicServiceWebClient(
        @Qualifier(CLIENT_HTTP_CONNECTOR) connector: ClientHttpConnector
    ) = WebClient.builder()
        .clientConnector(connector)
        .baseUrl(musicServiceUrl)
        .build()

    @Bean(CLIENT_HTTP_CONNECTOR)
    fun connector(): ClientHttpConnector {
        val provider = ConnectionProvider.builder("webclient-connection-pool")
            .maxConnections(100)
            .pendingAcquireTimeout(Duration.ofSeconds(10))
            .pendingAcquireMaxCount(-1)
            .maxIdleTime(Duration.ofMinutes(1))
            .build()
        val client = HttpClient.create(provider)

        return ReactorClientHttpConnector(client)
    }

    @Bean(DATABASE_CLIENT)
    fun databaseClient(factory: ConnectionFactory) = DatabaseClient.builder()
        .connectionFactory(factory)
        .namedParameters(true)
        .build()

    companion object {
        const val AUTH_SERVICE_WEB_CLIENT = "authServiceWebClient"
        const val MUSIC_SERVICE_WEB_CLIENT = "musicServiceWebClient"
        private const val CLIENT_HTTP_CONNECTOR = "clientHttpConnector"
        private const val DATABASE_CLIENT = "databaseClient"
    }
}