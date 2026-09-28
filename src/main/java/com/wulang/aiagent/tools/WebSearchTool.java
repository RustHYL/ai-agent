package com.wulang.aiagent.tools;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.wulang.aiagent.tools.retry.AgentRemoteToolRetry;
import io.github.resilience4j.retry.Retry;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class WebSearchTool {

    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    private static final int TIMEOUT_MILLIS = 8_000;

    private final String apiKey;

    private final Retry retry;

    public WebSearchTool(String apiKey) {
        this(apiKey, Retry.of(AgentRemoteToolRetry.NAME, AgentRemoteToolRetry.config()));
    }

    @Autowired
    public WebSearchTool(@Value("${search-api.api-key}") String apiKey, Retry agentRemoteToolRetry) {
        this.apiKey = apiKey;
        this.retry = agentRemoteToolRetry;
    }

    @Tool(description = "Search for information from Baidu Search Engine")
    public String searchWeb(@ToolParam(description = "Search query keyword") String query) {
        try {
            return retry.executeSupplier(() -> doSearch(query));
        } catch (Exception e) {
            return AgentRemoteToolRetry.failureMessage("搜索", e);
        }
    }

    private String doSearch(String query) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("q", query);
        paramMap.put("api_key", apiKey);
        paramMap.put("engine", "baidu");
        String response = HttpRequest.get(SEARCH_API_URL)
                .form(paramMap)
                .timeout(TIMEOUT_MILLIS)
                .execute()
                .body();
        JSONObject jsonObject = JSONUtil.parseObj(response);
        JSONArray organicResults = jsonObject.getJSONArray("organic_results");
        if (organicResults == null || organicResults.isEmpty()) {
            return "No search results";
        }
        int limit = Math.min(5, organicResults.size());
        List<Object> objects = organicResults.subList(0, limit);
        return objects.stream()
                .map(obj -> ((JSONObject) obj).toString())
                .collect(Collectors.joining(","));
    }
}
