package com.auth_service.global.util

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.InitializingBean
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.stereotype.Component
import sun.jvm.hotspot.oops.CellTypeState.value
import java.time.Duration

@Component
class RedisUtils(private val template: RedisTemplate<String, Any>): InitializingBean {

    companion object {
        private lateinit var operation: ValueOperations<String, Any>
        private val om = jacksonObjectMapper()

        fun saveValue(key: String, value: String, duration: Duration?) =
            getValue(key) ?: run {
                duration?.let { operation.set(key, value, it) }
                    ?: operation.set(key, value)
            }

        @Throws(JsonProcessingException::class)
        fun saveJson(key: String, json: Any, duration: Duration?) =
            getValue(key) ?: run {
                val jsonStr = om.writeValueAsString(json)
                duration?.let { operation.set(key, jsonStr, it) }
                    ?: operation.set(key, jsonStr)
            }

        fun getValue(key: String): String? = operation[key] as? String

        @Throws(JsonProcessingException::class)
        fun <T> getJson(key: String, clazz: Class<T>): T {
            val jsonStr = operation[key] as? String
            return jsonStr?.takeIf { it.isNotBlank() }.let {
                om.readValue(it, clazz)
            } ?: throw IllegalArgumentException("Value is not Present by Key : $key")
        }

        fun updateValue(key: String, value: String, duration: Duration) {
            deleteValue(key)
            saveValue(key, value, duration)
        }

        fun deleteValue(key: String): Any? = operation.getAndDelete(key)
    }

    override fun afterPropertiesSet() {
        operation = template.opsForValue()
    }
}