package com.sound_bind.kafka_server.domain.persistence.repository

import com.sound_bind.kafka_server.domain.persistence.entity.OrchestrationProcessStep
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OrchestrationProcessStepRepository: ReactiveCrudRepository<OrchestrationProcessStep, UUID>