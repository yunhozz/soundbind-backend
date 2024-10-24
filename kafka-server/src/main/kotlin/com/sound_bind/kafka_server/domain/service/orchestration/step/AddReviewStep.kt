package com.sound_bind.kafka_server.domain.service.orchestration.step

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sound_bind.global.dto.OrchestrationRequestDTO
import com.sound_bind.global.utils.KafkaConstants
import com.sound_bind.global.utils.logger
import com.sound_bind.kafka_server.domain.persistence.entity.ProcessStepStatus
import com.sound_bind.kafka_server.domain.persistence.entity.ProcessStepType
import org.springframework.kafka.core.KafkaTemplate
import reactor.core.publisher.Mono

class AddReviewStep(
    private val kafkaTemplate: KafkaTemplate<String, Map<String, Any>>
): ProcessStep() {

    private val logger = logger()
    private val mapper = jacksonObjectMapper()

    override fun process(request: OrchestrationRequestDTO): Mono<Boolean> {
        logger.info("Add Review Step Start")
        val kafkaEvent = request.event
        return Mono.fromFuture(
            kafkaTemplate.send(
                KafkaConstants.MUSIC_REVIEW_ADDED_TOPIC,
                mapper.convertValue(kafkaEvent.message, object : TypeReference<Map<String, Any>>() {})
            ).handle { result, ex ->
                logger.info(result.toString())
                if (ex == null) {
                    this.status = ProcessStepStatus.COMPLETED
                    this.type = ProcessStepType.PROCESS
                    this.error = ""
                    true
                } else {
                    this.status = ProcessStepStatus.FAILED
                    this.type = ProcessStepType.ROLLBACK
                    this.error = ex.localizedMessage
                    false
                }
            }
        )
    }

    override fun revert(): Mono<Boolean> {
        TODO("Not yet implemented")
    }

    override fun copyStep(): ProcessStep {
        TODO("Not yet implemented")
    }
}