package com.sound_bind.notification_service.global.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
@EnableKafka
class KafkaConsumerConfig {

    companion object {
        private const val BOOTSTRAP_SERVERS = "localhost:9090, localhost:9091, localhost:9092"
        private const val CONSUMER_FACTORY = "consumerFactory"
        private const val LISTENER_CONTAINER_FACTORY = "listenerContainerFactory"
    }

    @Bean(CONSUMER_FACTORY)
    fun consumerFactory(): ConsumerFactory<String, Map<String, String>> {
        val config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to BOOTSTRAP_SERVERS,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
        )
        return DefaultKafkaConsumerFactory(config)
    }

    @Bean(LISTENER_CONTAINER_FACTORY)
    fun listenerContainerFactory(@Qualifier(CONSUMER_FACTORY) factory: ConsumerFactory<String, Map<String, String>>) =
        object: ConcurrentKafkaListenerContainerFactory<String, Map<String, String>>() {
            init {
                consumerFactory = factory
            }
        }
}