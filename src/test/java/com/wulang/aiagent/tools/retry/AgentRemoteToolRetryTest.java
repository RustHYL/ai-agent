package com.wulang.aiagent.tools.retry;

import cn.hutool.core.io.IORuntimeException;
import io.github.resilience4j.retry.Retry;
import org.jsoup.HttpStatusException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentRemoteToolRetryTest {

    @Test
    void retriesTransientIoFailureThenSucceeds() {
        Retry retry = Retry.of("test", AgentRemoteToolRetry.config(Duration.ofMillis(1)));
        AtomicInteger calls = new AtomicInteger();

        String result = retry.executeSupplier(() -> {
            if (calls.incrementAndGet() < 3) {
                throw new IORuntimeException(new IOException("connection reset"));
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(3, calls.get());
    }

    @Test
    void doesNotRetryIllegalArgument() {
        Retry retry = Retry.of("test", AgentRemoteToolRetry.config(Duration.ofMillis(1)));
        AtomicInteger calls = new AtomicInteger();

        assertThrows(IllegalArgumentException.class, () -> retry.executeSupplier(() -> {
            calls.incrementAndGet();
            throw new IllegalArgumentException("bad query");
        }));
        assertEquals(1, calls.get());
    }

    @Test
    void retriesServerStatusButNotNotFound() {
        assertTrue(AgentRemoteToolRetry.isRetryable(new HttpStatusException("unavailable", 503, "https://example.com")));
        assertTrue(AgentRemoteToolRetry.isRetryable(new HttpStatusException("too many", 429, "https://example.com")));
        assertFalse(AgentRemoteToolRetry.isRetryable(new HttpStatusException("missing", 404, "https://example.com")));
        assertFalse(AgentRemoteToolRetry.isRetryable(new IllegalArgumentException("bad")));
    }
}
