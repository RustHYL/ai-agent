package com.wulang.aiagent.agent.model;

import lombok.Getter;

/**
 * 智能体对外展示的回复类别。
 */
@Getter
public enum AgentOutputType {

    /**
     * 对当前任务的分析、拆解和下一步规划。
     */
    THOUGHT("思考"),

    /**
     * 调用外部工具或执行代码的指令。
     */
    ACTION("行动"),

    /**
     * 工具执行后的原始结果。
     */
    OBSERVATION("观察"),

    /**
     * 总结后的最终答案。
     */
    ANSWER("最终交付");

    private final String label;

    AgentOutputType(String label) {
        this.label = label;
    }
}
