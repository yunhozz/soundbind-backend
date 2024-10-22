package com.sound_bind.kafka_server.domain.service.dto

import com.sound_bind.kafka_server.domain.persistence.entity.OrchestrationProcessStep

data class OrchestrationProcessDTO(
    val id: String?,
    val status: String?,
    val processSteps: List<OrchestrationProcessStep>
)