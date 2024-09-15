package com.sound_bind.kafka_server.global.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
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
import org.springframework.kafka.transaction.KafkaTransactionManager

@Configuration
@EnableKafka
class KafkaConfig {

    @Bean(KAFKA_TEMPLATE)
    fun kafkaTemplate(@Qualifier(PRODUCER_FACTORY) factory: ProducerFactory<String, Map<String, Any>>) = KafkaTemplate(factory)

    @Bean(TX_KAFKA_TEMPLATE)
    fun transactionKafkaTemplate(@Qualifier(TX_PRODUCER_FACTORY) factory: ProducerFactory<String, Map<String, Any>>) = KafkaTemplate(factory)

    @Configuration
    class KafkaProducerConfig {

        @Value("\${spring.kafka.bootstrap-servers}")
        private lateinit var bootstrapServers: List<String>

        @Bean(PRODUCER_FACTORY)
        fun producerFactory(): ProducerFactory<String, Map<String, Any>> {
            val config = mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
            )
            return DefaultKafkaProducerFactory(config)
        }

        @Bean(TX_PRODUCER_FACTORY)
        fun transactionProducerFactory(): ProducerFactory<String, Map<String, Any>> {
            val config = mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true,
                ProducerConfig.TRANSACTIONAL_ID_CONFIG to "kafka-producer-transaction"
            )
            val producerFactory = DefaultKafkaProducerFactory<String, Map<String, Any>>(config)
            producerFactory.setTransactionIdPrefix("trx-")
            return producerFactory
        }

        @Bean(KAFKA_TX_MANAGER)
        fun kafkaTransactionManager(@Qualifier(TX_PRODUCER_FACTORY) factory: ProducerFactory<String, Map<String, Any>>) =
            KafkaTransactionManager(factory)
    }

    @Configuration
    class KafkaConsumerConfig {

        @Value("\${spring.kafka.bootstrap-servers}")
        private lateinit var bootstrapServers: List<String>

        @Bean(CONSUMER_FACTORY)
        fun consumerFactory(): ConsumerFactory<String, Map<String, Any>> {
            val config = mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
            )
            return DefaultKafkaConsumerFactory(config)
        }

        @Bean(LISTENER_CONTAINER_FACTORY)
        fun listenerContainerFactory(@Qualifier(CONSUMER_FACTORY) factory: ConsumerFactory<String, Map<String, Any>>) =
            object: ConcurrentKafkaListenerContainerFactory<String, Map<String, Any>>() {
                init {
                    consumerFactory = factory
                }
            }
    }

    companion object {
        const val KAFKA_TEMPLATE = "kafkaTemplate"
        const val TX_KAFKA_TEMPLATE = "kafkaTransactionTemplate"
        private const val PRODUCER_FACTORY = "producerFactory"
        private const val TX_PRODUCER_FACTORY = "transactionProducerFactory"
        private const val KAFKA_TX_MANAGER = "kafkaTransactionManager"
        private const val CONSUMER_FACTORY = "consumerFactory"
        private const val LISTENER_CONTAINER_FACTORY = "listenerContainerFactory"
    }
}