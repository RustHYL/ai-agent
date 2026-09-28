package com.wulang.aiagent.tools;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 集中的工具注册类
 */
@Configuration
public class ToolRegistration {

    @Bean
    public ToolCallback[] allTools(WebSearchTool webSearchTool,
                                   WebScrapingTool webScrapingTool,
                                   ResourceDownloadTool resourceDownloadTool) {
        FileOperationTool fileOperationTool = new FileOperationTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
//        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        TerminateTool terminateTool = new TerminateTool();
        return ToolCallbacks.from(
                fileOperationTool,
                webSearchTool,
                webScrapingTool,
                resourceDownloadTool,
                terminalOperationTool,
//                pdfGenerationTool,
                terminateTool
        );
    }
}

