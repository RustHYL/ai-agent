package com.wulang.aiagent.tools.retry;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.http.HttpException;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;
import org.jsoup.HttpStatusException;

import java.io.IOException;
import java.time.Duration;

/**
 * 远程工具的重试判定。只覆盖连接超时、网络中断和 5xx 这类瞬时失败。
 */
public final class AgentRemoteToolRetry {

    public static final String NAME = "agentRemoteTool";

    private static final Duration INITIAL_WAIT = Duration.ofMillis(400);

    private static final int MAX_ATTEMPTS = 3;

    private AgentRemoteToolRetry() {
    }

    public static RetryConfig config() {
        return config(INITIAL_WAIT);
    }

    static RetryConfig config(Duration initialWait) {
        return RetryConfig.custom()
                .maxAttempts(MAX_ATTEMPTS)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(initialWait, 2))
                .retryOnException(AgentRemoteToolRetry::isRetryable)
                .failAfterMaxAttempts(true)
                .build();
    }

    /**
     * 4xx（除 408、429）和参数错误不重试，避免把确定性失败放大成三次调用。
     */
    public static boolean isRetryable(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof InterruptedException) {
                return false;
            }
            if (current instanceof HttpStatusException statusException) {
                return isRetryableStatus(statusException.getStatusCode());
            }
            if (current instanceof IOException || current instanceof IORuntimeException || current instanceof HttpException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    public static String failureMessage(String action, Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null && root.getCause() != root) {
            if (root instanceof IOException || root instanceof IORuntimeException || root instanceof HttpException) {
                break;
            }
            root = root.getCause();
        }
        return action + "失败，已重试结束：" + root.getMessage();
    }

    private static boolean isRetryableStatus(int statusCode) {
        return statusCode == 408 || statusCode == 429 || statusCode >= 500;
    }
}
