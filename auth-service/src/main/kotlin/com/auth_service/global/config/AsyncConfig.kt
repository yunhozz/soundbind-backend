package com.auth_service.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor
import java.util.concurrent.ThreadPoolExecutor

@Configuration
@EnableAsync
class AsyncConfig: AsyncConfigurer {

    companion object {
        const val THREAD_POOL_TASK_EXECUTOR = "threadPoolTaskExecutor"
    }

    @Bean(THREAD_POOL_TASK_EXECUTOR)
    override fun getAsyncExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor().apply {
            corePoolSize = 5
            maxPoolSize = 10
            queueCapacity = 20
            setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
            setThreadNamePrefix("auth-service-async-")
        }
        executor.initialize()
        return executor
    }
}