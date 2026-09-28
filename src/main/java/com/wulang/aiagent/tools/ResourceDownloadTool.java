package com.wulang.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.wulang.aiagent.constant.FileConstant;
import com.wulang.aiagent.tools.retry.AgentRemoteToolRetry;
import io.github.resilience4j.retry.Retry;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 资源下载工具
 */
@Component
public class ResourceDownloadTool {

    private static final int TIMEOUT_MILLIS = 8_000;

    private final Retry retry;

    public ResourceDownloadTool() {
        this(Retry.of(AgentRemoteToolRetry.NAME, AgentRemoteToolRetry.config()));
    }

    @Autowired
    public ResourceDownloadTool(Retry agentRemoteToolRetry) {
        this.retry = agentRemoteToolRetry;
    }

    @Tool(description = "Download a resource from a given URL")
    public String downloadResource(
            @ToolParam(description = "URL of the resource to download") String url,
            @ToolParam(description = "Name of the file to save the downloaded resource") String fileName) {
        String fileDir = FileConstant.File_SAVE_DIR + "/download";
        String filePath = fileDir + "/" + fileName;
        try {
            return retry.executeSupplier(() -> doDownload(url, fileDir, filePath));
        } catch (Exception e) {
            return AgentRemoteToolRetry.failureMessage("下载资源", e);
        }
    }

    private String doDownload(String url, String fileDir, String filePath) {
        FileUtil.mkdir(fileDir);
        HttpUtil.downloadFile(url, new File(filePath), TIMEOUT_MILLIS);
        return "Resource downloaded successfully to: " + filePath;
    }
}
