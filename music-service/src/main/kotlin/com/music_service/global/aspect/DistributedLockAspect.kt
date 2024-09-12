package com.music_service.global.aspect

import com.music_service.global.annotation.DistributedLock
import com.music_service.global.util.RedisUtils
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.dao.CannotAcquireLockException
import org.springframework.stereotype.Component
import java.util.UUID

@Aspect
@Component
class DistributedLockAspect {

    @Around("@annotation(com.music_service.global.annotation.DistributedLock)")
    fun distributedSimpleLock(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val distributedLock = method.getAnnotation(DistributedLock::class.java)

        val lockKey = distributedLock.key
        val lockValue = UUID.randomUUID().toString()

        try {
            val acquired = RedisUtils.tryLock(
                lockKey,
                lockValue,
                distributedLock.leaseTime,
                distributedLock.timeUnit
            )
            if (!acquired) {
                throw CannotAcquireLockException("Failed to acquire lock for key: $lockKey")
            }
            return joinPoint.proceed()
        } finally {
            RedisUtils.releaseLock(lockKey, lockValue)
        }
    }
}