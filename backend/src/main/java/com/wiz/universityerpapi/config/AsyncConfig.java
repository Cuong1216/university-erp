package com.wiz.universityerpapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;

import java.util.Map;
import java.util.concurrent.Executor;
import org.slf4j.MDC;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "payrollTaskExecutor")
    public Executor payrollTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("PayrollAsync-");

        executor.setTaskDecorator(runnable -> {
            Map<String, String> mdcCopy = MDC.getCopyOfContextMap();
            return () -> {
                if (mdcCopy != null) MDC.setContextMap(mdcCopy);
                try { runnable.run(); } finally { MDC.clear(); }
            };
        });

        executor.initialize();

        return new DelegatingSecurityContextExecutor(executor);
    }
}
