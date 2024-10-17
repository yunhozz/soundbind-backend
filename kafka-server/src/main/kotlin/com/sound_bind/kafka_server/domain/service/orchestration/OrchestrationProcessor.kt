package com.sound_bind.kafka_server.domain.service.orchestration

import com.sound_bind.global.dto.OrchestrationRequestDTO
import com.sound_bind.global.dto.OrchestrationResponseDTO
import com.sound_bind.kafka_server.domain.persistence.entity.ProcessStatus
import com.sound_bind.kafka_server.domain.service.orchestration.step.ProcessStep
import reactor.core.publisher.Mono
import java.util.LinkedList

abstract class OrchestrationProcessor {

    var status: ProcessStatus = ProcessStatus.PENDING
    var processSteps: List<ProcessStep> = emptyList()
    var rollbackSteps: LinkedList<ProcessStep> = LinkedList(emptyList())

    abstract fun process(dto: OrchestrationRequestDTO): Mono<OrchestrationResponseDTO>
    abstract fun revert(processor: OrchestrationProcessor, dto: OrchestrationRequestDTO): Mono<OrchestrationResponseDTO>
    abstract fun getOnSuccessResponseDTO(dto: OrchestrationRequestDTO): OrchestrationResponseDTO
    abstract fun getOnFailResponseDTO(dto: OrchestrationRequestDTO): OrchestrationResponseDTO
}