package com.wulang.aiagent.tools.retry;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 搜索、抓取、下载共用同一个 Resilience4j 重试实例：最多 3 次，间隔 400ms 起按 2 倍递增。
 */
@Configuration
@Slf4j
public class AgentRemoteToolRetryConfig {

    @Bean
    public Retry agentRemoteToolRetry(RetryRegistry retryRegistry) {
        Retry retry = retryRegistry.retry(AgentRemoteToolRetry.NAME, AgentRemoteToolRetry.config());
        retry.getEventPublisher()
                .onRetry(event -> log.warn("远程工具 {} 第 {} 次重试，原因：{}",
                        event.getName(),
                        event.getNumberOfRetryAttempts(),
                        event.getLastThrowable() == null ? "" : event.getLastThrowable().getMessage()))
                .onError(event -> log.error("远程工具 {} 重试后仍失败", event.getName(), event.getLastThrowable()));
        return retry;
    }
}
