package com.music_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.sound_bind.global", "com.music_service"])
class MusicServiceApplication

fun main(args: Array<String>) {
	runApplication<MusicServiceApplication>(*args)
}
