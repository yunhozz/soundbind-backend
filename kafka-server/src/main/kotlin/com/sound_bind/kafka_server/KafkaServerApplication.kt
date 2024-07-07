package com.sound_bind.kafka_server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KafkaServerApplication

fun main(args: Array<String>) {
	runApplication<KafkaServerApplication>(*args)
}
