package com.auth_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.sound_bind.global", "com.auth_service"])
class AuthServiceApplication

fun main(args: Array<String>) {
	runApplication<AuthServiceApplication>(*args)
}
