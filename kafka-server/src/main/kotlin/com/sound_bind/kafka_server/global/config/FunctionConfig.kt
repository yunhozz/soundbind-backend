package com.sound_bind.kafka_server.global.config

import com.sound_bind.kafka_server.domain.service.KafkaService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FunctionConfig(
    private val kafkaService: KafkaService
) {
    @Bean
    fun kafka(): (String) -> Unit = kafkaService.publish()

    @Bean
    fun orchestrate(): (String) -> Unit = kafkaService.publishOnSaga()
}