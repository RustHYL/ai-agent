package com.wulang.aiagent.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;



/**
 * 网页抓取工具
 */

public class WebScrapingTool {
    @Tool(description = "scrape the content of the web page")
    public String scrapeWeb(@ToolParam(description = "URL of the web page to scrap") String url) {

        try {
            Document document = Jsoup.connect(url).get();
            return document.html();
        } catch (Exception e) {
            return "Error scraping web page: " + e.getMessage();
        }
    }
}
