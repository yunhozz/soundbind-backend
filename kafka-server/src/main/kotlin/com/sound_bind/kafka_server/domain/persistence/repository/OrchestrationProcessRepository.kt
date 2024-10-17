package com.sound_bind.kafka_server.domain.persistence.repository

import com.sound_bind.kafka_server.domain.persistence.entity.OrchestrationProcess
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface OrchestrationProcessRepository: ReactiveCrudRepository<OrchestrationProcess, String>