package com.muyuanjin.feel.dmn;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Singular;
import jakarta.annotation.Nullable;

import java.util.List;

/**
 * DMN决策表定义
 *
 * @param name        决策表名称
 * @param inputs      表头——输入条款
 * @param outputs     表头——输出条款
 * @param annotations 表头——注释条款
 * @param rules       规则列表，行索引
 * @param hitPolicy   规则命中策略
 * @param aggregation 多值命中策略选择的聚合器
 */
@Builder
public record DecisionTableDefinition(
        @NotEmpty String name,
        @NotEmpty @Singular List<InputClause> inputs,
        @NotEmpty @Singular List<OutputClause> outputs,
        @Nullable @Singular List<String> annotations,
        @Nullable @Singular List<DecisionRule> rules,
        @Nullable HitPolicy hitPolicy,
        @Nullable Aggregation aggregation) {
    public DecisionTableDefinition {
        if (rules != null && !rules.isEmpty()) {
            for (DecisionRule decisionRule : rules) {
                if (decisionRule.inputEntry().size() != inputs.size()) {
                    throw new IllegalArgumentException("规则输入数量与表头输入数量不一致");
                }
            }
            for (DecisionRule rule : rules) {
                if (rule.outputEntry().size() != outputs.size()) {
                    throw new IllegalArgumentException("规则输出数量与表头输出数量不一致");
                }
            }
        }
    }
}