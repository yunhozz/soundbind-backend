package com.sound_bind.kafka_server.domain.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("orchestration_process_step")
class OrchestrationProcessStep(
    @Id
    val id: UUID,
    val orchestratorProcessId: UUID,
    val name: String,
    val stepStatus: String,
    val stepType: String,
    val error: String
)

enum class ProcessStepStatus {
    COMPLETED,
    PENDING,
    FAILED
}

enum class ProcessStepType {
    PROCESS,
    ROLLBACK
}