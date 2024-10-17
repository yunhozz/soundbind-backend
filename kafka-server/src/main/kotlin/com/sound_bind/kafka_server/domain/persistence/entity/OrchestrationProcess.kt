package com.sound_bind.kafka_server.domain.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("orchestration_process")
class OrchestrationProcess(
    @Id
    val id: String,
    val status: String
)

enum class ProcessStatus {
    COMPLETED,
    PENDING,
    FAILED;

    companion object {
        fun of(value: String): ProcessStatus = entries.first { it.name == value }
    }
}