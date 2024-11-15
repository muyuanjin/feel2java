package com.muyuanjin.feel.dmn;

import jakarta.annotation.Nullable;

/**
 * DMN表头的输入部分
 *
 * @param inputExpression 输入表达式
 * @param inputValues     如果不为null且 rule 中该Input 为 - 时，判断输入是否在此范围内，如果是则匹配（逻辑上等价于-替换为该值)
 */
public record InputClause(
        @Nullable String inputExpression,
        @Nullable String inputValues) {
    public InputClause() {
        this(null, null);
    }

    public InputClause(String inputExpression) {
        this(inputExpression, null);
    }
}