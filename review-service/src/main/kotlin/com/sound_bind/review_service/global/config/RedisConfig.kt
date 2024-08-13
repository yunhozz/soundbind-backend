package com.sound_bind.review_service.global.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@EnableCaching
class RedisConfig {

    companion object {
        private const val REDIS_TEMPLATE = "redisTemplate"
        private const val REDIS_CONNECTION_FACTORY = "redisConnectionFactory"
    }

    @Value("\${redis.host}")
    private lateinit var host: String

    @Value("\${redis.port}")
    private lateinit var port: String

    @Bean(REDIS_TEMPLATE)
    fun redisTemplate(@Qualifier(REDIS_CONNECTION_FACTORY) factory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = factory
        template.keySerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.valueSerializer = StringRedisSerializer()
        template.hashValueSerializer = StringRedisSerializer()
        return template
    }

    @Bean(REDIS_CONNECTION_FACTORY)
    fun redisConnectionFactory(): RedisConnectionFactory =
        LettuceConnectionFactory(RedisStandaloneConfiguration(host, port.toInt()))
}