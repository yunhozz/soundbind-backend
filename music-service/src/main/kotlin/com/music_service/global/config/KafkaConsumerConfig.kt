package com.music_service.global.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.converter.StringJsonMessageConverter
import org.springframework.util.backoff.FixedBackOff

@Configuration
@EnableKafka
class KafkaConsumerConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: List<String>

    @Bean(KAFKA_CONSUMER_FACTORY)
    fun kafkaConsumerFactory(): ConsumerFactory<String, Map<String, Any>> {
        val config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to AUTO_OFFSET_RESET_CONFIG
        )
        return DefaultKafkaConsumerFactory(config)
    }

    @Bean(KAFKA_LISTENER_CONTAINER_FACTORY)
    fun kafkaListenerContainerFactory(
        @Qualifier(KAFKA_CONSUMER_FACTORY) factory: ConsumerFactory<String, Map<String, Any>>,
        template: KafkaTemplate<String, Map<String, Any>>
    ) = object: ConcurrentKafkaListenerContainerFactory<String, Map<String, Any>>() {
        init {
            consumerFactory = factory
            containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
            setRecordMessageConverter(StringJsonMessageConverter())
            setCommonErrorHandler(DefaultErrorHandler(
                DeadLetterPublishingRecoverer(template) { record, exception ->
                    val log = LoggerFactory.getLogger("KafkaConsumerErrorHandler")
                    log.error(
                        "[Kafka Consumer Error] topic='{}', key='{}', value='{}', error message='{}'",
                        record.topic(),
                        record.key(),
                        record.value(),
                        exception.message
                    )
                    TopicPartition(record.topic() + ".dlc", record.partition())
                }, FixedBackOff(BACK_OFF_INTERVAL_MS, BACK_OFF_MAX_ATTEMPTS)
            ))
        }
    }

    companion object {
        private const val KAFKA_CONSUMER_FACTORY = "kafkaConsumerFactory"
        private const val KAFKA_LISTENER_CONTAINER_FACTORY = "kafkaListenerContainerFactory"
        private const val AUTO_OFFSET_RESET_CONFIG = "latest"
        private const val BACK_OFF_INTERVAL_MS = 1000L
        private const val BACK_OFF_MAX_ATTEMPTS = 5L
    }
}