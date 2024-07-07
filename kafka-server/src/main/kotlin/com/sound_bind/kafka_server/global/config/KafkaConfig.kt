package com.sound_bind.kafka_server.global.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
@EnableKafka
class KafkaConfig {

    companion object {
        private const val BOOTSTRAP_SERVERS = "localhost:9090, localhost:9091, localhost:9092"
        private const val PRODUCER_FACTORY = "producerFactory"
        private const val CONSUMER_FACTORY = "consumerFactory"
        private const val LISTENER_CONTAINER_FACTORY = "listenerContainerFactory"
        private const val KAFKA_TEMPLATE = "kafkaTemplate"
    }

    @Bean(KAFKA_TEMPLATE)
    fun kafkaTemplate(
        @Qualifier(PRODUCER_FACTORY) factory: ProducerFactory<String, Any>
    ): KafkaTemplate<String, Any> = KafkaTemplate(factory)

    @Configuration
    class KafkaProducerConfig {

        @Bean(PRODUCER_FACTORY)
        fun producerFactory(): ProducerFactory<String, Any> {
            val config = hashMapOf<String, Any>(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to BOOTSTRAP_SERVERS,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
            )
            return DefaultKafkaProducerFactory(config)
        }
    }

    @Configuration
    class KafkaConsumerConfig {

        @Bean(CONSUMER_FACTORY)
        fun consumerFactory(): ConsumerFactory<String, Any> {
            val config = hashMapOf<String, Any>(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to BOOTSTRAP_SERVERS,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
            )
            return DefaultKafkaConsumerFactory(config)
        }

        @Bean(LISTENER_CONTAINER_FACTORY)
        fun listenerContainerFactory(
            @Qualifier(CONSUMER_FACTORY) factory: ConsumerFactory<String, Any>
        ): ConcurrentKafkaListenerContainerFactory<String, Any> =
            object: ConcurrentKafkaListenerContainerFactory<String, Any>() {
                init {
                    consumerFactory = factory
                }
            }
    }
}