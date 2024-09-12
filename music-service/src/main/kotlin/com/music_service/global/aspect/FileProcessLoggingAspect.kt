package com.music_service.global.aspect

import com.music_service.global.annotation.LogMessage
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class FileProcessLoggingAspect {
    private val log: Logger = LoggerFactory.getLogger(FileProcessLoggingAspect::class.java)

    @Around("execution(* com.music_service.domain.application.manager.impl.FileManagerImpl.*(..))")
    fun logAround(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method

        val logMessageAnnotation = method.getAnnotation(LogMessage::class.java)
        val message = logMessageAnnotation?.message ?: "Executing File Manager..."
        val methodName = method.name

        log.info("$methodName() started: $message")

        val result = joinPoint.proceed()

        log.info("$methodName() finished")

        return result
    }
}