package com.music_service.global.config

import com.music_service.global.handler.KafkaConsumerErrorHandler
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
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.retrytopic.RetryTopicConfiguration
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy
import org.springframework.kafka.support.EndpointHandlerMethod
import org.springframework.kafka.support.serializer.JsonDeserializer

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
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false
        )
        return DefaultKafkaConsumerFactory(config)
    }

    @Bean(KAFKA_LISTENER_CONTAINER_FACTORY)
    fun kafkaListenerContainerFactory(@Qualifier(KAFKA_CONSUMER_FACTORY) factory: ConsumerFactory<String, Map<String, Any>>) =
        object: ConcurrentKafkaListenerContainerFactory<String, Map<String, Any>>() {
            init {
                consumerFactory = factory
                containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
            }
        }

    @Bean(KAFKA_RETRY_TOPIC_CONFIG)
    fun kafkaRetryTopicConfig(
        @Qualifier(KAFKA_LISTENER_CONTAINER_FACTORY) listenerFactory: ConcurrentKafkaListenerContainerFactory<String, Map<String, Any>>,
        template: KafkaTemplate<String, Any>
    ): RetryTopicConfiguration = RetryTopicConfigurationBuilder()
        .autoCreateTopicsWith(KafkaConsumerConstants.REPLICA_COUNT, KafkaConsumerConstants.REPLICATION_FACTOR)
        .maxAttempts(KafkaConsumerConstants.MAX_ATTEMPT_COUNT)
        .fixedBackOff(KafkaConsumerConstants.BACK_OFF_PERIOD)
        .listenerFactory(listenerFactory)
        .setTopicSuffixingStrategy(TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
        .dltHandlerMethod(EndpointHandlerMethod(KafkaConsumerErrorHandler::class.java, "postProcessDltMessage"))
        .create(template)

    companion object {
        private const val KAFKA_CONSUMER_FACTORY = "kafkaConsumerFactory"
        private const val KAFKA_LISTENER_CONTAINER_FACTORY = "kafkaListenerContainerFactory"
        private const val KAFKA_RETRY_TOPIC_CONFIG = "kafkaRetryTopicConfig"
    }

    private object KafkaConsumerConstants {
        const val REPLICA_COUNT: Int = 5
        const val REPLICATION_FACTOR: Short = 3
        const val MAX_ATTEMPT_COUNT: Int = 3
        const val BACK_OFF_PERIOD: Int = 5
    }
}