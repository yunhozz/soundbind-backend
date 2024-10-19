package com.sound_bind.kafka_server.domain.service

import com.sound_bind.global.dto.OrchestrationRequestDTO
import com.sound_bind.global.dto.OrchestrationResponseDTO
import com.sound_bind.global.utils.logger
import com.sound_bind.kafka_server.domain.persistence.entity.OrchestrationProcess
import com.sound_bind.kafka_server.domain.persistence.entity.OrchestrationProcessStep
import com.sound_bind.kafka_server.domain.persistence.entity.ProcessStatus
import com.sound_bind.kafka_server.domain.persistence.repository.OrchestrationProcessRepository
import com.sound_bind.kafka_server.domain.persistence.repository.OrchestrationProcessStepRepository
import com.sound_bind.kafka_server.domain.service.dto.OrchestrationProcessDTO
import com.sound_bind.kafka_server.domain.service.orchestration.OrchestrationProcessor
import com.sound_bind.kafka_server.domain.service.orchestration.step.ProcessStep
import io.r2dbc.spi.Row
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class OrchestrationService(
    private val orchestrationProcessRepository: OrchestrationProcessRepository,
    private val orchestrationProcessStepRepository: OrchestrationProcessStepRepository,
    private val orchestrationProcessor: OrchestrationProcessor
) {

    private val logger = logger()

    companion object {
        private val MAPPING_FUNCTION: (Row) -> OrchestrationProcessDTO = { row ->
            OrchestrationProcessDTO(
                row.get("id", String::class.java),
                row.get("status", String::class.java),
                emptyList()
            )
        }

        private val MAPPING_FUNCTION_STEP: (Row) -> OrchestrationProcessStep = { row ->
            OrchestrationProcessStep(
                row.get("id", String::class.java)!!,
                row.get("orchestrator_process_id", String::class.java)!!,
                row.get("name", String::class.java)!!,
                row.get("status_step", String::class.java)!!,
                row.get("step_type", String::class.java)!!,
                row.get("error", String::class.java)!!
            )
        }
    }

    fun processOrchestration(): (Flux<OrchestrationRequestDTO>) -> Flux<OrchestrationResponseDTO> = { flux ->
        flux.flatMap { req ->
            orchestrationProcessor.process(req)
                .flatMap { res ->
                    val orchestrationProcess = OrchestrationProcess(res.id, res.status)
                    orchestrationProcessRepository.save(orchestrationProcess)
                }
                .flatMap { process ->
                    logger.info("Process recorded with id: ${process.id}")
                    if (ProcessStatus.of(process.status) == ProcessStatus.COMPLETED) {
                        insertSteps(process.id!!, orchestrationProcessor.processSteps)
                    } else {
                        insertSteps(process.id!!, orchestrationProcessor.rollbackSteps)
                    }
                }
                .then(Mono.just(orchestrationProcessor.getOnSuccessResponseDTO(req)))
        }
    }

    private fun insertSteps(
        processId: String,
        steps: List<ProcessStep>
    ): Mono<Void> {
        val orchestrationProcessSteps = steps.map { step ->
            OrchestrationProcessStep(
                UUID.randomUUID().toString(),
                processId,
                step.javaClass.name,
                step.status.name,
                step.type.name,
                step.error
            )
        }

        return orchestrationProcessStepRepository.saveAll(orchestrationProcessSteps)
            .doOnNext {
                logger.info("Process steps recorded with process id: ${it.orchestratorProcessId}")
            }
            .then()
    }
}