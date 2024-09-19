package com.music_service.global.handler

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component

@Component
class KafkaConsumerErrorHandler {
    private val log: Logger = LoggerFactory.getLogger(KafkaConsumerErrorHandler::class.java)

    fun postProcessDltMessage(
        record: ConsumerRecord<String, String>,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        @Header(KafkaHeaders.EXCEPTION_MESSAGE) exceptionMessage: String,
        @Header(KafkaHeaders.GROUP_ID) groupId: String
    ) {
        log.error("[DLT Log] received message='{}' with offset='{}', topic='{}', groupId='{}'", record.value(), offset, topic, groupId)
    }
}