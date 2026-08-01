package com.wiz.universityerpapi.core.config;

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
            // Capture TenantContext on the request thread before submitting to the thread pool
            String tenantId = com.wiz.universityerpapi.tenant.TenantContext.getTenantId();
            return () -> {
                if (mdcCopy != null) MDC.setContextMap(mdcCopy);
                // Restore TenantContext on the async thread
                com.wiz.universityerpapi.tenant.TenantContext.setTenantId(tenantId);
                try { 
                    runnable.run(); 
                } finally { 
                    MDC.clear(); 
                    // Crucial: clear TenantContext to prevent data leaks when thread returns to the pool
                    com.wiz.universityerpapi.tenant.TenantContext.clear();
                }
            };
        });

        executor.initialize();

        return new DelegatingSecurityContextExecutor(executor);
    }
}
