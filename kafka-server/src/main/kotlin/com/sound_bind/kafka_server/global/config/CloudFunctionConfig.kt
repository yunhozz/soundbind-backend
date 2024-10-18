package com.sound_bind.kafka_server.global.config

import com.sound_bind.global.dto.OrchestrationRequestDTO
import com.sound_bind.global.dto.OrchestrationResponseDTO
import com.sound_bind.kafka_server.domain.service.KafkaService
import com.sound_bind.kafka_server.domain.service.OrchestrationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Flux

@Configuration
class CloudFunctionConfig(
    private val kafkaService: KafkaService,
    private val orchestrationService: OrchestrationService
) {
    @Bean
    fun kafka(): (String) -> Unit = kafkaService.publishEvent()

    @Bean
    fun process(): (Flux<OrchestrationRequestDTO>) -> Flux<OrchestrationResponseDTO> =
        orchestrationService.processOrchestration()
}