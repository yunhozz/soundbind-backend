package com.sound_bind.kafka_server.domain.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sound_bind.global.dto.KafkaEvent
import com.sound_bind.global.dto.OrchestrationRequestDTO
import com.sound_bind.global.utils.logger
import com.sound_bind.kafka_server.domain.persistence.entity.OrchestrationProcess
import com.sound_bind.kafka_server.domain.persistence.entity.OrchestrationProcessStep
import com.sound_bind.kafka_server.domain.persistence.entity.ProcessStatus
import com.sound_bind.kafka_server.domain.persistence.repository.OrchestrationProcessRepository
import com.sound_bind.kafka_server.domain.persistence.repository.OrchestrationProcessStepRepository
import com.sound_bind.kafka_server.domain.service.orchestration.ReviewAddedOrchestrationProcessor
import com.sound_bind.kafka_server.domain.service.orchestration.step.ProcessStep
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@Service
class KafkaService(
    private val kafkaTemplate: KafkaTemplate<String, Map<String, Any>>,
    private val orchestrationProcessRepository: OrchestrationProcessRepository,
    private val orchestrationProcessStepRepository: OrchestrationProcessStepRepository
) {
    private val mapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private val logger = logger()

    fun publish(): (String) -> Unit = { event ->
        val data = if (event.trim().startsWith("[")) {
            mapper.readValue(event, List::class.java)
        } else {
            listOf(mapper.readValue(event, Map::class.java))
        }
        logger.info("Received Data : $data")

        val threads = data.size
        val executorService = Executors.newFixedThreadPool(threads)
        val latch = CountDownLatch(threads)

        for (d in data) {
            executorService.execute {
                val payload = mapper.readValue(mapper.writeValueAsString(d), Map::class.java)
                val topic = payload["topic"].toString()
                val message = payload["message"]

                val kafkaMessage = mapper.readValue(
                    mapper.writeValueAsString(message),
                    object: TypeReference<Map<String, Any>>() {}
                )
                kafkaTemplate.send(topic, kafkaMessage)
                latch.countDown()
            }
        }

        executorService.shutdown()
        latch.await()
    }

    fun publishOnSaga(): (String) -> Unit = { req ->
        val events = listOf(mapper.readValue(req, object : TypeReference<Map<String, Any>>() {}))
        Flux.fromIterable(events)
            .doOnNext { event -> logger.info("event = $event") }
            .flatMap { event ->
                val orchestrationProcessor = ReviewAddedOrchestrationProcessor(kafkaTemplate)
                val kafkaEvent = mapper.convertValue(event, KafkaEvent::class.java)
                val request = OrchestrationRequestDTO(UUID.randomUUID().toString(), kafkaEvent)
                logger.info("kafka event = $kafkaEvent")
                orchestrationProcessor.process(request)
                    .flatMap { response ->
                        logger.info("response = $response")
                        val orchestrationProcess = OrchestrationProcess(response.id, response.status)
                        orchestrationProcessRepository.save(orchestrationProcess)
                            .flatMap { process ->
                                logger.info("Process recorded with id: ${process.id}")
                                if (ProcessStatus.of(process.status) == ProcessStatus.COMPLETED) {
                                    insertSteps(process.id!!, orchestrationProcessor.processSteps)
                                } else {
                                    insertSteps(process.id!!, orchestrationProcessor.rollbackSteps)
                                }
                            }.thenReturn(response)
                    }
            }
            .subscribe()
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
                logger.info("Process steps recorded with process id: ${it.orchestrationProcessId}")
            }
            .then()
    }
}