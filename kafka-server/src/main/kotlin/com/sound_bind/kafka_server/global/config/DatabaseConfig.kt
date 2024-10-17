package com.sound_bind.kafka_server.global.config

import io.asyncer.r2dbc.mysql.MySqlConnectionFactoryProvider
import org.springframework.boot.autoconfigure.r2dbc.ConnectionFactoryOptionsBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.ZoneId

@Configuration
class DatabaseConfig {

    @Bean
    fun mysqlCustomizer(): ConnectionFactoryOptionsBuilderCustomizer =
        ConnectionFactoryOptionsBuilderCustomizer {
            it.option(MySqlConnectionFactoryProvider.SERVER_ZONE_ID, ZoneId.of("Asia/Seoul"))
        }
}