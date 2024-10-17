package com.sound_bind.kafka_server.global.config

import com.sound_bind.global.dto.OrchestrationRequestDTO
import com.sound_bind.global.dto.OrchestrationResponseDTO
import com.sound_bind.kafka_server.domain.service.OrchestrationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Flux

@Configuration
class OrchestrationConfig(
    private val orchestrationService: OrchestrationService
) {

    @Bean
    fun process(): (Flux<OrchestrationRequestDTO>) -> Flux<OrchestrationResponseDTO> =
        orchestrationService.processOrchestration()
}