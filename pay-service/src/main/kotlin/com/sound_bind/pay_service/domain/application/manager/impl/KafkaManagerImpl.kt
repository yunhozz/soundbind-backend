package com.sound_bind.pay_service.domain.application.manager.impl

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sound_bind.pay_service.domain.application.manager.KafkaManager
import org.springframework.stereotype.Component

@Component
class KafkaManagerImpl: KafkaManager {

    companion object {
        private val mapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        const val PAY_SERVICE_GROUP = "pay-service-group"
        const val USER_ADDED_TOPIC = "user-added-topic"
    }
}