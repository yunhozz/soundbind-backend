package com.auth_service.global.annotation

import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(
    val key: String,
    val leaseTime: Long = 10,
    val timeUnit: TimeUnit = TimeUnit.SECONDS
)