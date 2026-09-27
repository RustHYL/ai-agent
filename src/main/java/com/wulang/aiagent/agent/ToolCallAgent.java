package com.wulang.aiagent.agent;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.wulang.aiagent.agent.model.AgentOutputType;
import com.wulang.aiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 处理工具调用的基础代理类，实现think 和 act
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent{

    // 可用的工具
    private final ToolCallback[] availableTools;

    // 保存工具调用信息的响应结果（要调用的工具）
    private ChatResponse toolCallChatResponse;

    // 管理工具调用
    private final ToolCallingManager toolCallingManager;

    // 警用Spring ai 的内置工具调用机制，自己维护选项和上下文
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools){
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = DashScopeChatOptions.builder()
                // 警用Spring ai 的内置工具调用机制，自己维护选项和上下文
                .withProxyToolCalls(true)
                .build();
    }

    private boolean nextStepPromptAdded = false;

    /**
     * 处理当前状态并决定下一步
     * @return 是否要执行行动（调用工具）
     */
    @Override
    public boolean think() {
        // 下一步提示词只写入一次，避免每个步骤重复占用上下文
        if (!nextStepPromptAdded && StrUtil.isNotBlank(getNextStepPrompt())) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
            nextStepPromptAdded = true;
        }

        // 调用 AI 大模型，获取工具调用结果（需要调用的工具列表）
        List<Message> messageList = getMessageList();
        log.info("MessageList=======" + getMessageList().toString());
        Prompt prompt = new Prompt(messageList, this.chatOptions);
        try {
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(this.availableTools)
                    .call()
                    .chatResponse();
            // 3.解析工具调用结果，获取要调用的工具
            this.toolCallChatResponse = chatResponse;
            // 助手消息
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCalls = assistantMessage.getToolCalls();
            if (toolCalls == null) {
                toolCalls = List.of();
            }
            log.info("{}的思考：{}", getName(), result);
            log.info("{}选择了工具：{}个", getName(), toolCalls.size());
            List<AssistantMessage.ToolCall> businessCalls = toolCalls.stream()
                    .filter(toolCall -> !"doTerminate".equals(toolCall.name()))
                    .toList();
            boolean terminateCalled = toolCalls.size() != businessCalls.size();
            String toolCallInfo = businessCalls.stream()
                    .map(toolCall -> "调用 " + toolCall.name() + "\n参数：" + toolCall.arguments())
                    .collect(Collectors.joining("\n\n"));
            log.info(toolCallInfo);
            // 没有业务工具：正文就是最终答案。终止工具只用来结束循环，不展示给用户
            if (businessCalls.isEmpty()) {
                getMessageList().add(assistantMessage);
                if (StrUtil.isNotBlank(result)) {
                    addOutput(AgentOutputType.ANSWER, result);
                    setState(AgentState.FINISHED);
                } else if (terminateCalled) {
                    addOutput(AgentOutputType.ANSWER, "任务已结束");
                    setState(AgentState.FINISHED);
                }
                return false;
            }
            addOutput(AgentOutputType.THOUGHT, result);
            addOutput(AgentOutputType.ACTION, toolCallInfo);
            return true;
            //异常处理
        } catch (Exception e) {
            log.error(getName() + "的工具思考过程虚线问题：" + e.getMessage());
            getMessageList().add(new AssistantMessage("处理时遇到了错误：" + e.getMessage()));
            addOutput(AgentOutputType.THOUGHT, "处理时遇到了错误：" + e.getMessage());
            return false;
        }
    }

    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "没有工具需要调用";
        }
        Prompt prompt = new Prompt(getMessageList(), this.chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        // 记录上下文conversionHistory已经包含助手消息和工具调用结果
        setMessageList(toolExecutionResult.conversationHistory());
        //当前工具调用结果
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        // 判断是否调用了终止工具
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> response.name().equals("doTerminate"));
        if (terminateToolCalled) {
            // 结束
            setState(AgentState.FINISHED);
        }
        String result = toolResponseMessage.getResponses().stream()
                .filter(response -> !"doTerminate".equals(response.name()))
                .map(response -> response.name() + "：\n" + response.responseData())
                .collect(Collectors.joining("\n\n"));
        log.info(result);
        addOutput(AgentOutputType.OBSERVATION, result);
        return result;
    }
}

