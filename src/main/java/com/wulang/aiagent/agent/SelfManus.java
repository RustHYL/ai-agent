package com.wulang.aiagent.agent;

import com.wulang.aiagent.advisor.MyLogAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

@Component
public class SelfManus extends ToolCallAgent {

    public SelfManus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        this.setName("SelfManus");
        String SYSTEM_PROMPT = """  
                You are SelfManus, an all-capable AI assistant, aimed at solving any task presented by the user.  
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.  
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """  
                Based on user needs, proactively select the most appropriate tool or combination of tools.  
                For complex tasks, break the problem down and use different tools step by step.  
                Before calling any tool, write your analysis, the task breakdown, and the next action in the assistant text, then call the tools.  
                After each tool result, use the observation to decide the next step.  
                When the task is complete, reply with the polished final answer for the user and do not call any tool.  
                Call `doTerminate` only when you cannot proceed further, and do not call it together with other tools.  
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxSteps(20);
        // 初始化客户端
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLogAdvisor())
                .build();

        this.setChatClient(chatClient);
    }
}
