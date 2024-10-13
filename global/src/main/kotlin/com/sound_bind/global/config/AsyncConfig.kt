package com.sound_bind.global.config

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

    /*
    최소 풀 사이즈 : Tn x (Cm - 1) + 1
    여유 풀 사이즈 : Tn x (Cm - 1) + (Tn x 2)

    Tn = Thread Count
    Cm = Simultaneous Connection Count
     */

    companion object {
        private const val THREAD_COUNT = 8
        private const val SIMULTANEOUS_CONNECTION_COUNT = 100
        private const val QUEUE_CAPACITY = 50
    }

    @Bean
    override fun getAsyncExecutor(): Executor {
        val maxPoolSize = THREAD_COUNT * (SIMULTANEOUS_CONNECTION_COUNT - 1) + (THREAD_COUNT * 2)
        val executor = ThreadPoolTaskExecutor().apply {
            corePoolSize = THREAD_COUNT
            queueCapacity = QUEUE_CAPACITY
            this.maxPoolSize = maxPoolSize
            setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
        }
        executor.initialize()
        return executor
    }
}