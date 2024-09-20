package com.sound_bind.review_service.global.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.converter.StringJsonMessageConverter

@Configuration
@EnableKafka
class KafkaConsumerConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: List<String>

    @Bean(CONSUMER_FACTORY)
    fun consumerFactory(): ConsumerFactory<String, Map<String, Any>> {
        val config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest"
        )
        return DefaultKafkaConsumerFactory(config)
    }

    @Bean(LISTENER_CONTAINER_FACTORY)
    fun listenerContainerFactory(@Qualifier(CONSUMER_FACTORY) factory: ConsumerFactory<String, Map<String, Any>>) =
        object: ConcurrentKafkaListenerContainerFactory<String, Map<String, Any>>() {
            init {
                consumerFactory = factory
                containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
                setRecordMessageConverter(StringJsonMessageConverter())
            }
        }

    companion object {
        private const val CONSUMER_FACTORY = "consumerFactory"
        private const val LISTENER_CONTAINER_FACTORY = "listenerContainerFactory"
    }
}