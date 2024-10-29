package com.sound_bind.kafka_server.global.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.CustomConversions
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.DialectResolver
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.core.DatabaseClient
import java.nio.ByteBuffer
import java.util.UUID

@Configuration
@EnableR2dbcRepositories
class R2DBCConfig {

    @Bean
    fun databaseClient(factory: ConnectionFactory) = DatabaseClient.builder()
        .connectionFactory(factory)
        .namedParameters(true)
        .build()

    @Bean
    fun customConversion(databaseClient: DatabaseClient): CustomConversions {
        val dialect = DialectResolver.getDialect(databaseClient.connectionFactory)
        val converters = ArrayList(dialect.converters)
        val customConverters = listOf(UUID2BinaryConverter(), Binary2UUIDConverter())
        converters.addAll(R2dbcCustomConversions.STORE_CONVERTERS)

        return R2dbcCustomConversions(
            CustomConversions.StoreConversions.of(dialect.simpleTypeHolder, converters),
            customConverters
        )
    }
}

@WritingConverter
class UUID2BinaryConverter: Converter<UUID, ByteArray> {
    override fun convert(source: UUID): ByteArray {
        val byteBuffer = ByteBuffer.wrap(ByteArray(16))
        byteBuffer.putLong(source.mostSignificantBits)
        byteBuffer.putLong(source.leastSignificantBits)
        return byteBuffer.array()
    }
}

@ReadingConverter
class Binary2UUIDConverter: Converter<ByteArray, UUID> {
    override fun convert(source: ByteArray): UUID {
        val byteBuffer = ByteBuffer.wrap(source)
        val mostSignificantBits = byteBuffer.long
        val leastSignificantBits = byteBuffer.long
        return UUID(mostSignificantBits, leastSignificantBits)
    }
}