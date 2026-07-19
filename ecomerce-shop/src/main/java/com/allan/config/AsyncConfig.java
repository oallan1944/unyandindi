package com.allan.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Backing infrastructure for {@code @Async} event listeners
 * ({@code CouponRedeemedEventListener}, {@code PromotionChangedEventListener}).
 *
 * <p><strong>Why a named bounded executor instead of Spring's default:</strong>
 * without this, {@code @Async} falls back to {@code SimpleAsyncTaskExecutor},
 * which spawns an unbounded new thread per task — no reuse, no queue, no
 * backpressure. Under load that's a resource leak waiting to happen. This
 * bean gives listeners a fixed, reusable pool with a bounded queue and a
 * {@code CallerRunsPolicy} fallback (slows the publisher down under
 * sustained overload rather than dropping events or throwing
 * {@code RejectedExecutionException}).
 *
 * <p><strong>Uncaught exception handler:</strong> exceptions thrown inside a
 * {@code void}-returning {@code @Async} method are otherwise swallowed —
 * they never reach a caller's try/catch because there's no caller waiting
 * synchronously. Without this handler they'd only ever show up as a stray
 * log line from the executor itself, easy to miss. This routes them through
 * structured logging instead.
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("promo-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (Throwable ex, Method method, Object... params) ->
                log.error("Uncaught exception in async method '{}' with args {}", method.getName(), params, ex);
    }
}