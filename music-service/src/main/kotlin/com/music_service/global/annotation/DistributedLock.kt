package com.music_service.global.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class DistributedLock(
    val key: String,
    val waitTime: Long = 5000L,
    val leaseTime: Long = 1000L
)