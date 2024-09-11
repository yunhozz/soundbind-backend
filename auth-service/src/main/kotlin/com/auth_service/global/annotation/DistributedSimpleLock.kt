package com.auth_service.global.annotation

import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedSimpleLock(
    val key: String,
    val waitTime: Long = 5,
    val leaseTime: Long = 10,
    val timeUnit: TimeUnit = TimeUnit.MILLISECONDS
)