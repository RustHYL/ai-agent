package com.wulang.aiagent.agent.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 智能体单条结构化输出。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentOutput implements Serializable {

    private AgentOutputType type;

    private String content;

    /**
     * 所属步骤，从 1 开始。由运行循环补上。
     */
    private Integer step;

    public AgentOutput(AgentOutputType type, String content) {
        this.type = type;
        this.content = content;
    }

    public String getLabel() {
        return type == null ? "" : type.getLabel();
    }
}
