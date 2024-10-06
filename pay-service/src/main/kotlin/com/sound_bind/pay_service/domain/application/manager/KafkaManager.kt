package com.sound_bind.pay_service.domain.application.manager

interface KafkaManager {
    fun sendSponsorReceivedTopic(receiverId: Long, content: String, link: String?)
}