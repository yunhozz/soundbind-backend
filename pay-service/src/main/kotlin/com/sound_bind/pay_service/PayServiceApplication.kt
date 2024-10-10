package com.sound_bind.pay_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.sound_bind.*"])
class PayServiceApplication

fun main(args: Array<String>) {
	runApplication<PayServiceApplication>(*args)
}
