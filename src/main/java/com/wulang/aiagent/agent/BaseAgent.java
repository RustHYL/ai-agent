package com.wulang.aiagent.agent;

import com.itextpdf.styledxmlparser.jsoup.internal.StringUtil;
import com.wulang.aiagent.agent.model.AgentOutput;
import com.wulang.aiagent.agent.model.AgentOutputType;
import com.wulang.aiagent.agent.model.AgentState;
import com.wulang.aiagent.common.ResultUtils;
import com.wulang.aiagent.exception.ErrorCode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程。
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能。
 * 子类必须实现step方法。
 */
@Data
@Slf4j
public abstract class BaseAgent {

    // 核心属性
    private String name;

    // 提示
    private String systemPrompt;
    private String nextStepPrompt;

    // 状态
    private AgentState state = AgentState.IDLE;

    // 执行控制
    private int maxSteps = 10;
    private int currentStep = 0;

    // LLM
    private ChatClient chatClient;

    // Memory（需要自主维护会话上下文）
    private List<Message> messageList = new ArrayList<>();

    /**
     * 运行代理
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StringUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
        // 更改状态
        state = AgentState.RUNNING;
        // 记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        // 保存结果列表
        List<String> results = new ArrayList<>();
        try {
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step " + stepNumber + "/" + maxSteps);
                // 单步执行
                String result = formatStepResult(stepNumber, step());
                results.add(result);
            }
            // 检查是否超出步骤限制
            if (currentStep >= maxSteps && state != AgentState.FINISHED) {
                state = AgentState.FINISHED;
                results.add("[最终交付] 执行结束，已达到最大步骤（" + maxSteps + "）");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            return "执行错误" + e.getMessage();
        } finally {
            // 清理资源
            this.cleanup();
        }
    }

    /**
     * 运行代理(流式输出)
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public SseEmitter runStream(String userPrompt) {
        SseEmitter sseEmitter = new SseEmitter(300000L);
        // 使用线程异步处理 避免阻塞主线程
        CompletableFuture.runAsync(() -> {
            try {
                if (this.state != AgentState.IDLE) {
                    sseEmitter.send(ResultUtils.error(ErrorCode.OPERATION_ERROR, "错误，无法从这个状态运行代理：" + this.state));
                    sseEmitter.complete();
                    return;
                }
                if (StringUtil.isBlank(userPrompt)) {
                    sseEmitter.send(ResultUtils.error(ErrorCode.PARAMS_ERROR, "错误，不能运用空提示词进行代理（Cannot run agent with empty user prompt）"));
                    sseEmitter.complete();
                    return;
                }
            } catch (Exception e) {
                // 校验阶段发送失败：结束连接并退出，避免继续执行步骤循环
                sseEmitter.completeWithError(e);
                return;
            }
            // 校验通过后才进入运行
            state = AgentState.RUNNING;
            // 记录消息上下文
            messageList.add(new UserMessage(userPrompt));
            // 保存结果列表
            List<String> results = new ArrayList<>();
            try {
                for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                    int stepNumber = i + 1;
                    currentStep = stepNumber;
                    log.info("Executing step " + stepNumber + "/" + maxSteps);
                    // 单步执行，按思考 / 行动 / 观察 / 最终交付分别推送
                    List<AgentOutput> outputs = step();
                    for (AgentOutput output : outputs) {
                        output.setStep(stepNumber);
                        results.add(formatOutput(output));
                        sseEmitter.send(ResultUtils.success(output));
                    }
                }
                // 正常结束不会走到这里；只有步骤用尽仍未交付时才提示
                if (currentStep >= maxSteps && state != AgentState.FINISHED) {
                    state = AgentState.FINISHED;
                    AgentOutput limitOutput = new AgentOutput(AgentOutputType.ANSWER,
                            "执行结束，已达到最大步骤（" + maxSteps + "）", currentStep);
                    results.add(formatOutput(limitOutput));
                    sseEmitter.send(ResultUtils.success(limitOutput));
                }
                sseEmitter.complete();
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("Error executing agent", e);
                try {
                    sseEmitter.send(ResultUtils.error(ErrorCode.SYSTEM_ERROR, "执行错误" + e.getMessage()));
                    sseEmitter.complete();
                } catch (IOException ex) {
                    sseEmitter.completeWithError(ex);
                }
            } finally {
                // 清理资源
                this.cleanup();
            }
        });
        // 设置超时回调
        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("SSE connection timeout！");
        });
        sseEmitter.onCompletion(() -> {
            if (this.state == AgentState.FINISHED) {
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE connection completed!");
        });

        return sseEmitter;

    }

    /**
     * 执行单个步骤
     *
     * @return 本步产生的结构化输出
     */
    public abstract List<AgentOutput> step();

    private String formatStepResult(int stepNumber, List<AgentOutput> outputs) {
        if (outputs == null || outputs.isEmpty()) {
            return "Step " + stepNumber + ": （本步没有可展示的内容）";
        }
        String body = outputs.stream()
                .map(output -> {
                    output.setStep(stepNumber);
                    return formatOutput(output);
                })
                .collect(Collectors.joining("\n"));
        return "Step " + stepNumber + ":\n" + body;
    }

    private String formatOutput(AgentOutput output) {
        return "[" + output.getLabel() + "] " + output.getContent();
    }

    /**
     * 清理资源
     */
    protected void cleanup() {
        // 子类可以重写此方法来清理资源
    }
}
