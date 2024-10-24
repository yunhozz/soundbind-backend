package com.sound_bind.kafka_server.domain.service.orchestration

import com.sound_bind.global.dto.KafkaEvent
import com.sound_bind.global.dto.OrchestrationRequestDTO
import com.sound_bind.global.dto.OrchestrationResponseDTO
import com.sound_bind.global.utils.logger
import com.sound_bind.kafka_server.domain.persistence.entity.ProcessStatus
import com.sound_bind.kafka_server.domain.persistence.entity.ProcessStepStatus
import com.sound_bind.kafka_server.domain.persistence.entity.ProcessStepType
import com.sound_bind.kafka_server.domain.service.orchestration.step.AddReviewStep
import com.sound_bind.kafka_server.domain.service.orchestration.step.CheckMusicExistStep
import com.sound_bind.kafka_server.domain.service.orchestration.step.NotificationStep
import org.springframework.kafka.core.KafkaTemplate
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.LinkedList
import java.util.UUID

class ReviewAddedOrchestrationProcessor(
    kafkaTemplate: KafkaTemplate<String, Map<String, Any>>
): OrchestrationProcessor() {

    private val logger = logger()

    init {
        val checkMusicExistStep = CheckMusicExistStep(kafkaTemplate) // 음원 존재 여부 확인
        val addReviewStep = AddReviewStep(kafkaTemplate) // 리뷰 작성 및 음원의 리뷰 카운트 증가
        val notificationStep = NotificationStep(kafkaTemplate) // 알림 전송
        this.processSteps.addAll(listOf(checkMusicExistStep, addReviewStep, notificationStep))
    }

    override fun process(req: OrchestrationRequestDTO): Mono<OrchestrationResponseDTO> =
        Flux.fromStream { this.processSteps.stream() }
            .flatMap { step -> step.process(req) }
            .handle { result, sink ->
                if (result) {
                    sink.next(true)
                } else {
                    sink.error(Exception("Process Failed"))
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

                revert(this, req)
            }

    override fun revert(processor: OrchestrationProcessor, req: OrchestrationRequestDTO): Mono<OrchestrationResponseDTO> =
        Flux.fromStream { processor.rollbackSteps.stream() }
            .flatMap { it.revert() }
            .retry(3)
            .then(Mono.fromCallable {
                OrchestrationResponseDTO(UUID.randomUUID().toString(), ProcessStatus.FAILED.name)
            })

    override fun getOnSuccessResponseDTO(req: OrchestrationRequestDTO): OrchestrationResponseDTO =
        OrchestrationResponseDTO(req.id, ProcessStatus.COMPLETED.name)

    override fun getOnFailResponseDTO(req: OrchestrationRequestDTO): OrchestrationResponseDTO =
        OrchestrationResponseDTO(req.id, ProcessStatus.FAILED.name)
}