package cn.clazs.easymeeting.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置类
 * 用于配置@Async注解的线程池
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    /**
     * 用户信息异步更新线程池
     * 用于异步执行用户登录时间、退出时间等非关键业务更新
     *
     * 线程池配置说明：
     * - corePoolSize: 2，核心线程数，用户信息更新频率不高，2个足够
     * - maxPoolSize: 5，最大线程数，应对偶尔的并发高峰
     * - queueCapacity: 100，队列容量，缓冲异步任务
     * - keepAliveSeconds: 60，空闲线程存活时间
     * - threadNamePrefix: userInfo-async-，线程名前缀，便于日志追踪和问题排查
     */
    @Bean(name = "asyncTaskExecutor")
    public Executor userInfoAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数：一直存活的线程数
        executor.setCorePoolSize(3);

        // 最大线程数：线程池最多创建的线程数
        executor.setMaxPoolSize(5);

        // 队列容量：等待执行的任务队列大小
        executor.setQueueCapacity(100);

        // 线程名前缀：方便在日志中识别和追踪
        executor.setThreadNamePrefix("async-task-");

        // 空闲线程存活时间（秒）：超过核心线程数的空闲线程在60秒后会被回收
        executor.setKeepAliveSeconds(60);

        // 拒绝策略：当队列满了且线程数达到最大值时的处理策略
        // CallerRunsPolicy：由调用线程（提交任务的线程）处理该任务
        // 这样可以保证任务不丢失，但会降低调用线程的执行速度
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间（秒）：线程池最多等待30秒让任务完成
        executor.setAwaitTerminationSeconds(30);

        // 初始化线程池
        executor.initialize();

        log.info("异步任务服务线程池初始化完成 - coreSize: {}, maxSize: {}, queueCapacity: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }
}
