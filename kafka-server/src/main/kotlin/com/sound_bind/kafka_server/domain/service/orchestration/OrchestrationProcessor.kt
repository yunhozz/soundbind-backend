package com.sound_bind.kafka_server.domain.service.orchestration

import com.sound_bind.global.dto.OrchestrationRequestDTO
import com.sound_bind.global.dto.OrchestrationResponseDTO
import com.sound_bind.kafka_server.domain.persistence.entity.ProcessStatus
import com.sound_bind.kafka_server.domain.service.orchestration.step.ProcessStep
import reactor.core.publisher.Mono
import java.util.LinkedList

abstract class OrchestrationProcessor {

    var status: ProcessStatus = ProcessStatus.PENDING
    var processSteps: LinkedList<ProcessStep> = LinkedList()
    lateinit var rollbackSteps: LinkedList<ProcessStep>

    abstract fun process(req: OrchestrationRequestDTO): Mono<OrchestrationResponseDTO>
    abstract fun revert(processor: OrchestrationProcessor, req: OrchestrationRequestDTO): Mono<OrchestrationResponseDTO>
    abstract fun getOnSuccessResponseDTO(req: OrchestrationRequestDTO): OrchestrationResponseDTO
    abstract fun getOnFailResponseDTO(req: OrchestrationRequestDTO): OrchestrationResponseDTO
}