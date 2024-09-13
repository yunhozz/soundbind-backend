package com.music_service.global.aspect

import com.music_service.global.annotation.DistributedLock
import com.music_service.global.util.RedisUtils
import jakarta.persistence.LockTimeoutException
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.dao.CannotAcquireLockException
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.stereotype.Component
import java.util.UUID

@Aspect
@Component
class DistributedLockAspect {

    private val parser = SpelExpressionParser()

    @Around("@annotation(com.music_service.global.annotation.DistributedLock)")
    fun distributedLock(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val distributedLock = method.getAnnotation(DistributedLock::class.java)

        val context = StandardEvaluationContext().apply {
            setVariable("userId", joinPoint.args[0])
        }
        val lockKey = parser.parseExpression(distributedLock.key)
            .getValue(context, String::class.java)
            ?: throw IllegalStateException("Lock key cannot be null")
        val lockValue = UUID.randomUUID().toString()

        var retryCount = 0
        var acquired = false
        while (retryCount < 3) {
            acquired = RedisUtils.tryLock(
                lockKey,
                lockValue,
                distributedLock.leaseTime,
                distributedLock.timeUnit
            )
            if (acquired) break
            retryCount++
            Thread.sleep(10)
        }
        if (!acquired) {
            throw CannotAcquireLockException("Failed to acquire lock for key: $lockKey")
        }

        try {
            return joinPoint.proceed()
        } finally {
            val released = RedisUtils.releaseLock(lockKey, lockValue)
            if (!released) {
                throw LockTimeoutException("Failed to release lock for key: $lockKey")
            }
        }
    }
}