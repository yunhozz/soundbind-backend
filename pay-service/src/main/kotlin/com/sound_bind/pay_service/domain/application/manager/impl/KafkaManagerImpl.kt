package com.sound_bind.pay_service.domain.application.manager.impl

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sound_bind.pay_service.domain.application.manager.KafkaManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class KafkaManagerImpl: KafkaManager {

    companion object {
        private val mapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        const val PAY_SERVICE_GROUP = "pay-service-group"
        const val USER_ADDED_TOPIC = "user-added-topic"
        const val USER_DELETION_TOPIC = "user-deletion-topic"
        private const val SPONSOR_RECEIVED_TOPIC = "sponsor-received-topic"
    }

    @Value("\${uris.kafka-server-uri:http://localhost:9000}/api/kafka")
    private lateinit var kafkaRequestUri: String

    override fun sendSponsorReceivedTopic(receiverId: Long, pointAmount: Int) {
        TODO("Not yet implemented")
    }

    data class KafkaRequestDTO(
        val topic: String,
        val message: KafkaMessage
    ) {
        data class KafkaNotificationDTO(
            val userId: Long,
            val content: String,
            val link: String?
        ): KafkaMessage

        sealed interface KafkaMessage
    }
}