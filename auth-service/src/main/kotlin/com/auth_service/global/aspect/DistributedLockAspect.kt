package com.auth_service.global.aspect

import com.auth_service.global.annotation.DistributedLock
import com.auth_service.global.util.RedisUtils
import jakarta.persistence.LockTimeoutException
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

    @Around("@annotation(com.auth_service.global.annotation.DistributedLock)")
    fun distributedSimpleLock(joinPoint: ProceedingJoinPoint): Any {
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
            val released = RedisUtils.releaseLock(lockKey, lockValue)
            if (!released) {
                throw LockTimeoutException("Failed to release lock for key: $lockKey")
            }
        }
    }
}