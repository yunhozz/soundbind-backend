package com.sound_bind.review_service.global.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(
    val key: String,
    val waitTime: Long = 5000L,
    val leaseTime: Long = 1000L
)