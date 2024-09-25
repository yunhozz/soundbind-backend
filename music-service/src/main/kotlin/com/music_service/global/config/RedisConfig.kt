package com.music_service.global.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.redisson.spring.cache.CacheConfig
import org.redisson.spring.cache.RedissonSpringCacheManager
import org.redisson.spring.data.connection.RedissonConnectionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@EnableCaching
class RedisConfig {

    companion object {
        private const val REDISSON_CLIENT = "redissonClient"
        private const val REDIS_TEMPLATE = "redisTemplate"
        private const val REDISSON_HOST_PREFIX = "redis://"
        private const val REDIS_CACHE_MANAGER = "redisCacheManager"
        const val ONE_MIN_CACHE = "one-min-cache"
        const val FIVE_MIN_CACHE = "five-min-cache"
        private const val TTL_ONE_MINUTE = 1L
        private const val TTL_FIVE_MINUTE = 5L
    }

    @Value("\${redis.host}")
    private lateinit var host: String

    @Value("\${redis.port}")
    private lateinit var port: String

    @Bean(REDISSON_CLIENT)
    fun redissonClient(): RedissonClient {
        val config = Config()
        config
            .useSingleServer()
            .setAddress("${REDISSON_HOST_PREFIX}$host:$port")
        return Redisson.create(config)
    }

    @Bean(REDIS_TEMPLATE)
    fun redisTemplate(@Qualifier(REDISSON_CLIENT) client: RedissonClient) =
        RedisTemplate<String, Any>().apply {
            connectionFactory = RedissonConnectionFactory(client)
            keySerializer = StringRedisSerializer()
            hashKeySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
            hashValueSerializer = StringRedisSerializer()
        }

    @Bean(REDIS_CACHE_MANAGER)
    fun redisCacheManager(@Qualifier(REDISSON_CLIENT) client: RedissonClient): CacheManager {
        val config = mapOf(
            ONE_MIN_CACHE to CacheConfig(TTL_ONE_MINUTE, 0),
            FIVE_MIN_CACHE to CacheConfig(TTL_FIVE_MINUTE, 0)
        )
        return RedissonSpringCacheManager(client, config)
    }
}