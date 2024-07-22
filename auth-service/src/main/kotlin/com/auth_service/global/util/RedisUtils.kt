package com.auth_service.global.util

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.InitializingBean
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisUtils(private val template: RedisTemplate<String, Any>): InitializingBean {

    companion object {
        private lateinit var operation: ValueOperations<String, Any>
        private val om = ObjectMapper()

        fun saveValue(key: String, value: String) = operation.set(key, value)

        fun saveValue(key: String, value: String, duration: Duration) = operation.set(key, value, duration)

        @Throws(JsonProcessingException::class)
        fun saveJson(key: String, json: Any) {
            val jsonStr = om.writeValueAsString(json)
            operation.set(key, jsonStr)
        }

        fun getValue(key: String): String? = operation[key] as? String

        @Throws(JsonProcessingException::class)
        fun <T> getJson(key: String, clazz: Class<T>): T {
            val jsonStr = operation[key] as? String
            return jsonStr?.takeIf { it.isNotBlank() }.let {
                om.readValue(it, clazz)
            } ?: throw IllegalArgumentException("Value is not Present by Key : $key")
        }

        fun deleteValue(key: String) = operation.getAndDelete(key)
    }

    override fun afterPropertiesSet() {
        operation = template.opsForValue()
    }
}