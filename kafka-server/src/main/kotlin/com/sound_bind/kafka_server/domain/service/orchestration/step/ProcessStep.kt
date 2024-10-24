package com.sound_bind.kafka_server.domain.service.orchestration.step

import com.sound_bind.global.dto.OrchestrationRequestDTO
import com.sound_bind.kafka_server.domain.persistence.entity.ProcessStepStatus
import com.sound_bind.kafka_server.domain.persistence.entity.ProcessStepType
import reactor.core.publisher.Mono

abstract class ProcessStep {

    lateinit var status: ProcessStepStatus
    lateinit var type: ProcessStepType
    lateinit var error: String

    abstract fun process(request: OrchestrationRequestDTO): Mono<Boolean>
    abstract fun revert(): Mono<Boolean>
    abstract fun copyStep(): ProcessStep
}