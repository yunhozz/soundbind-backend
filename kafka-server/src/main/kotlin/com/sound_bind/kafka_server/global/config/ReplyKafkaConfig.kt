package com.sound_bind.kafka_server.global.config

import com.sound_bind.global.config.KafkaConfig
import com.sound_bind.global.utils.KafkaConstants
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import java.time.Duration

@Configuration
class ReplyKafkaConfig {

    @Bean
    fun replyingKafkaTemplate(
        @Qualifier(KafkaConfig.KAFKA_PRODUCER_FACTORY) producerFactory: ProducerFactory<String, Map<String, Any>>,
        replyContainer: ConcurrentMessageListenerContainer<String, Map<String, Any>>
    ) = ReplyingKafkaTemplate(producerFactory, replyContainer).apply {
        setDefaultReplyTimeout(Duration.ofSeconds(10))
    }

    @Bean
    fun replyContainer(
        @Qualifier(KafkaConfig.KAFKA_CONSUMER_FACTORY) consumerFactory: ConsumerFactory<String, Map<String, Any>>
    ): ConcurrentMessageListenerContainer<String, Map<String, Any>> {
        val containerProperties = ContainerProperties(
            KafkaConstants.MUSIC_IS_EXIST_RESPONSE_TOPIC,
            KafkaConstants.MUSIC_REVIEW_ADDED_RESPONSE_TOPIC,
            KafkaConstants.REVIEW_ADDED_NOTIFICATION_RESPONSE_TOPIC
        )
        val container = ConcurrentMessageListenerContainer(consumerFactory, containerProperties)
        container.containerProperties.isMissingTopicsFatal = false

        return container
    }
}