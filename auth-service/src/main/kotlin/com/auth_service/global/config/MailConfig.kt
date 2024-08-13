package com.auth_service.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

@Configuration
class MailConfig {

    @Value("\${spring.mail.host}")
    private lateinit var host: String

    @Value("\${spring.mail.port}")
    private lateinit var port: String

    @Value("\${spring.mail.username}")
    private lateinit var username: String

    @Value("\${spring.mail.password}")
    private lateinit var password: String

    @Bean
    fun javaMailSender(): JavaMailSender {
        val javaMailSender = JavaMailSenderImpl()
        javaMailSender.host = host
        javaMailSender.port = port.toInt()
        javaMailSender.username = username
        javaMailSender.password = password
        return javaMailSender
    }
}