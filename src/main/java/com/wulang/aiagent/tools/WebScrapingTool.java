package com.wulang.aiagent.tools;

import cn.hutool.core.io.IORuntimeException;
import com.wulang.aiagent.tools.retry.AgentRemoteToolRetry;
import io.github.resilience4j.retry.Retry;
import org.jsoup.Jsoup;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 网页抓取工具
 */
@Component
public class WebScrapingTool {

    private static final int TIMEOUT_MILLIS = 8_000;

    private final Retry retry;

    public WebScrapingTool() {
        this(Retry.of(AgentRemoteToolRetry.NAME, AgentRemoteToolRetry.config()));
    }

    @Autowired
    public WebScrapingTool(Retry agentRemoteToolRetry) {
        this.retry = agentRemoteToolRetry;
    }

    @Tool(description = "scrape the content of the web page")
    public String scrapeWeb(@ToolParam(description = "URL of the web page to scrap") String url) {
        try {
            return retry.executeSupplier(() -> doScrape(url));
        } catch (Exception e) {
            return AgentRemoteToolRetry.failureMessage("抓取网页", e);
        }
    }

    private String doScrape(String url) {
        try {
            return Jsoup.connect(url).timeout(TIMEOUT_MILLIS).get().html();
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }
}
