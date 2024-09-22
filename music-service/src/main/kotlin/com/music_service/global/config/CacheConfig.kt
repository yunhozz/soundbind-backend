package com.music_service.global.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableCaching
class CacheConfig {

    @Bean(REDIS_CACHE_MANAGER)
    fun redisCacheManager() = RedisCacheManagerBuilderCustomizer {
        it
            .withCacheConfiguration(
                ONE_MIN_CACHE,
                redisCacheConfigurationByTtl(TTL_ONE_MINUTE)
            )
            .withCacheConfiguration(
                FIVE_MIN_CACHE,
                redisCacheConfigurationByTtl(TTL_FIVE_MINUTE)
            )
    }

    private fun redisCacheConfigurationByTtl(ttlInMin: Long) = RedisCacheConfiguration
        .defaultCacheConfig()
        .computePrefixWith { "$it::" }
        .entryTtl(Duration.ofMinutes(ttlInMin))
        .disableCachingNullValues()
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(
                StringRedisSerializer()
            )
        )
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(
                GenericJackson2JsonRedisSerializer(mapper)
            )
        )

    companion object {
        private val mapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        const val REDIS_CACHE_MANAGER = "redisCacheManager"
        const val ONE_MIN_CACHE = "one-min-cache"
        const val FIVE_MIN_CACHE = "five-min-cache"
        private const val TTL_ONE_MINUTE = 1L
        private const val TTL_FIVE_MINUTE = 5L
    }
}