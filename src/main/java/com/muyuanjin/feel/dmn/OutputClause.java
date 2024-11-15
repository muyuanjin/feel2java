package com.muyuanjin.feel.dmn;

import jakarta.annotation.Nullable;

/**
 * DMN表头的输出部分<P/>
 * Table 34: OutputClause attributes and model associations
 *
 * @param name               单输出判定表的输出条款不得指定名称，多输出判定表的输出条款必须指定名称
 * @param outputValues       输出的校验值列表，作为 UnaryTests 评估，如果输出值不在范围内则视为规则违规
 * @param defaultOutputEntry 输出的默认值,当匹配不到规则时（不完整表）作为输出结果
 */
public record OutputClause(
        @Nullable String name,
        @Nullable String outputValues,
        @Nullable String defaultOutputEntry) {
    public OutputClause() {
        this(null, null, null);
    }

    public OutputClause(String name) {
        this(name, null, null);
    }

    public OutputClause(String name, String outputValues) {
        this(name, outputValues, null);
    }
}