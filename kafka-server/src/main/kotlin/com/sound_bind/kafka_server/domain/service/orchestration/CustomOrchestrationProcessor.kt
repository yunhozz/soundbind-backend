package com.sound_bind.kafka_server.domain.service.orchestration

import com.sound_bind.global.dto.OrchestrationRequestDTO
import com.sound_bind.global.dto.OrchestrationResponseDTO
import com.sound_bind.global.utils.logger
import com.sound_bind.kafka_server.domain.persistence.entity.ProcessStatus
import com.sound_bind.kafka_server.domain.persistence.entity.ProcessStepStatus
import com.sound_bind.kafka_server.domain.persistence.entity.ProcessStepType
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.LinkedList
import java.util.UUID

@Component
class CustomOrchestrationProcessor: OrchestrationProcessor() {

    private val logger = logger()

    override fun process(dto: OrchestrationRequestDTO): Mono<OrchestrationResponseDTO> =
        Flux.fromStream { this.processSteps.stream() }
            .mapNotNull { step ->
                step.process()
                    .handle { result, sink ->
                        if (result) {
                            sink.next(true)
                        } else {
                            sink.error(Exception("Process Failed"))
                        }
                    }
            }
            .then(Mono.fromCallable {
                this.status = ProcessStatus.COMPLETED
                OrchestrationResponseDTO(UUID.randomUUID().toString(), ProcessStatus.COMPLETED.name)
            })
            .onErrorResume { err ->
                logger.info("Process Rollback", err)
                this.status = ProcessStatus.FAILED
                this.rollbackSteps = LinkedList(processSteps).apply {
                    removeIf { it.status != ProcessStepStatus.COMPLETED }
                    replaceAll { it.copyStep() }
                }
                this.rollbackSteps.forEach { it.type = ProcessStepType.ROLLBACK }

                revert(this, dto)
            }

    override fun revert(processor: OrchestrationProcessor, dto: OrchestrationRequestDTO): Mono<OrchestrationResponseDTO> =
        Flux.fromStream { processor.rollbackSteps.stream() }
            .flatMap { it.revert() }
            .retry(3)
            .then(Mono.fromCallable {
                OrchestrationResponseDTO(UUID.randomUUID().toString(), ProcessStatus.FAILED.name)
            })

    override fun getOnSuccessResponseDTO(dto: OrchestrationRequestDTO): OrchestrationResponseDTO =
        OrchestrationResponseDTO(dto.id, ProcessStatus.COMPLETED.name)

    override fun getOnFailResponseDTO(dto: OrchestrationRequestDTO): OrchestrationResponseDTO =
        OrchestrationResponseDTO(dto.id, ProcessStatus.FAILED.name)
}