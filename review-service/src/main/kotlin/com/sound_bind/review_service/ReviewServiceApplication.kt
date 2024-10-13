package com.sound_bind.review_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.sound_bind.*"])
class ReviewServiceApplication

fun main(args: Array<String>) {
	runApplication<ReviewServiceApplication>(*args)
}
