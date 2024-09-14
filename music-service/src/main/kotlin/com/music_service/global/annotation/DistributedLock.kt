package com.music_service.global.annotation

import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(
    val key: String,
    val leaseTime: Long,
    val timeUnit: TimeUnit = TimeUnit.SECONDS,
    val retryCount: Int = 0,
    val waitTimeMillis: Long = 0
)