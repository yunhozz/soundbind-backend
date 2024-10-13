package com.sound_bind.kafka_server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.sound_bind.*"])
class KafkaServerApplication

fun main(args: Array<String>) {
	runApplication<KafkaServerApplication>(*args)
}
