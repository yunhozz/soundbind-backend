package com.sound_bind.pay_service.domain.application.manager.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sound_bind.global.dto.KafkaEvent
import com.sound_bind.global.dto.KafkaMessage
import com.sound_bind.global.utils.KafkaConstants
import com.sound_bind.pay_service.domain.application.manager.KafkaManager
import khttp.post
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class KafkaManagerImpl: KafkaManager {

    private val mapper = jacksonObjectMapper()

    @Value("\${uris.kafka-server-uri:http://localhost:9000}/kafka")
    private lateinit var kafkaRequestUri: String

    override fun sendSponsorReceivedTopic(receiverId: Long, content: String, link: String?) {
        val sponsorReceivedEvent = KafkaEvent(
            topic = KafkaConstants.SPONSOR_RECEIVED_TOPIC,
            message = KafkaMessage.NotificationMessage(receiverId, content, link)
        )
        sendEventsToKafkaProducer(sponsorReceivedEvent)
    }

    private fun sendEventsToKafkaProducer(vararg events: KafkaEvent) =
        post(
            url = kafkaRequestUri,
            headers = mapOf("Content-Type" to "application/json"),
            data = mapper.writeValueAsString(events.toList())
        )
}