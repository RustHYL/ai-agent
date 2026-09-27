package com.wulang.aiagent.agent;

import com.wulang.aiagent.agent.model.AgentOutput;
import com.wulang.aiagent.agent.model.AgentOutputType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * ReAct (Reasoning and Acting) 模式的代理抽象类
 * 实现了思考-行动的循环模式
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ReActAgent extends BaseAgent {

    @EqualsAndHashCode.Exclude
    private final List<AgentOutput> stepOutputs = new ArrayList<>();

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动，true表示需要执行，false表示不需要执行
     */
    public abstract boolean think();

    /**
     * 执行决定的行动
     *
     * @return 行动执行结果
     */
    public abstract String act();

    /**
     * 记录本步要展示的一条输出。空白内容不进入结果。
     */
    protected void addOutput(AgentOutputType type, String content) {
        if (content == null || content.isBlank()) {
            return;
        }
        stepOutputs.add(new AgentOutput(type, content.strip()));
    }

    /**
     * 执行单个步骤：思考和行动
     *
     * @return 本步产生的结构化输出
     */
    @Override
    public List<AgentOutput> step() {
        stepOutputs.clear();
        try {
            boolean shouldAct = think();
            if (shouldAct) {
                act();
            }
            return List.copyOf(stepOutputs);
        } catch (Exception e) {
            e.printStackTrace();
            addOutput(AgentOutputType.THOUGHT, "步骤执行失败: " + e.getMessage());
            return List.copyOf(stepOutputs);
        }
    }
}
