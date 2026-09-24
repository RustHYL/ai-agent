package com.wulang.aiagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@MapperScan("com.wulang.aiagent.mapper")
public class AiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAgentApplication.class, args);
    }

//    @Bean
//    public ToolCallbackProvider toolCallbackProvider(/* Other dependencies */) {
//// Ensure the service passed in is not null
//        return MethodToolCallbackProvider.builder()
//                .toolObjects(/* Your service instance here */)
//                .build();
//    }

}
