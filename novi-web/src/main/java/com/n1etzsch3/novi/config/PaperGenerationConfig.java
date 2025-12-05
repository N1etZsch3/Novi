package com.n1etzsch3.novi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 组卷功能配置类
 * <p>
 * 配置线程池，用于并发生成套卷中的不同题型。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-12-04
 */
@Slf4j
@Configuration
public class PaperGenerationConfig {

    /**
     * 创建组卷专用线程池
     * <p>
     * 核心线程数：3<br>
     * 最大线程数：5<br>
     * 空闲线程存活时间：60秒<br>
     * 队列大小：10<br>
     * 拒绝策略：CallerRunsPolicy（调用者线程执行）
     * </p>
     */
    @Bean(name = "paperGenerationExecutor")
    public ThreadPoolExecutor paperGenerationExecutor() {
        log.info("Initializing paper generation thread pool");

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                3, // 核心线程数
                5, // 最大线程数
                60L, TimeUnit.SECONDS, // 空闲线程存活时间
                new LinkedBlockingQueue<>(10), // 队列大小
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("paper-gen-" + threadNumber.getAndIncrement());
                        return thread;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
        );

        log.info("Paper generation thread pool initialized: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                executor.getCorePoolSize(), executor.getMaximumPoolSize(), 10);

        return executor;
    }
}
