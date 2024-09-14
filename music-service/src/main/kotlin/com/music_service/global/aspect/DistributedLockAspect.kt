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
    private val context = StandardEvaluationContext()

    @Around("@annotation(com.music_service.global.annotation.DistributedLock)")
    fun distributedLock(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val distributedLock = method.getAnnotation(DistributedLock::class.java)

        val parameterNames = signature.parameterNames
        val parameterValues = joinPoint.args
        for (i in parameterNames.indices) {
            context.setVariable(parameterNames[i], parameterValues[i])
        }
        val lockKey = parser.parseExpression(distributedLock.key)
            .getValue(context, String::class.java)
            ?: throw IllegalStateException("Lock key cannot be null")
        val lockValue = UUID.randomUUID().toString()

        val leaseTime = distributedLock.leaseTime
        val timeUnit = distributedLock.timeUnit
        var retryCount = distributedLock.retryCount
        val waitTimeMills = distributedLock.waitTimeMillis
        var acquired = false
        while (retryCount >= 0) {
            retryCount --
            acquired = RedisUtils.tryLock(
                lockKey,
                lockValue,
                leaseTime,
                timeUnit
            )
            if (acquired) break
            Thread.sleep(waitTimeMills)
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