package com.sound_bind.review_service.global.aspect

import com.sound_bind.review_service.global.annotation.DistributedLock
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.redisson.api.RedissonClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1)
class DistributedLockAspect(
    private val redissonClient: RedissonClient
) {

    companion object {
        private val parser = SpelExpressionParser()
        private val context = StandardEvaluationContext()
        private val log: Logger = LoggerFactory.getLogger(DistributedLockAspect::class.java)
    }

    @Around("@annotation(com.sound_bind.review_service.global.annotation.DistributedLock)")
    fun redissonLock(joinPoint: ProceedingJoinPoint): Any? {
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
        val rLock = redissonClient.getLock(lockKey)

        try {
            val lockable = rLock.tryLock(distributedLock.waitTime, distributedLock.leaseTime, TimeUnit.MILLISECONDS)
            if (!lockable) {
                log.warn("Failed to acquire lock for key: $lockKey")
                return null
            }
            return joinPoint.proceed()

        } finally {
            try {
                rLock.unlock()
            } catch (e: IllegalMonitorStateException) {
                log.info("Redisson Lock Already UnLock {} {}", method.name, lockKey)
            }
        }
    }
}